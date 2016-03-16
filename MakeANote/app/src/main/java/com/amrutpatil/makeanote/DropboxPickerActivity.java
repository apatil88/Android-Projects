package com.amrutpatil.makeanote;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

/**
 * Description: Display and select directories from Dropbox.
 */
public class DropboxPickerActivity extends BaseActivity
        implements DropboxDirectoryListenerAsync.OnLoadFinished,
                   DropboxDirectoryCreatorAsyncTask.OnDirectoryCreateFinished
{
    private DropboxAPI<AndroidAuthSession> mApi;
    private boolean mAfterAuth = false;
    private DropboxAdapter mDropboxAdapter;
    private Stack<String>  mDirectoryStack = new Stack<>();
    private boolean mIsFirstClick = true;
    private ProgressDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dbx_picker_layout);
        mDirectoryStack.push("/");
        setUpList();
        setUpBar();
        setUpDirectoryCreator();

        if(!AppSharedPreferences.isDropBoxAuthenticated(getApplicationContext())){
            authenticate();
        }else{
            //If authenticated, display the root directory
            AndroidAuthSession session = DropboxActions.buildSession(getApplicationContext());
            mApi = new DropboxAPI<AndroidAuthSession>(session);
            initProgressDialog();
            //Kick of a process which gets the root directory from Dropbox
            new DropboxDirectoryListenerAsync(getApplicationContext(), mApi,
                    getCurrentPath(), DropboxPickerActivity.this).execute();
        }
    }

    private void setUpDirectoryCreator(){
        final EditText newDirectory = (EditText) findViewById(R.id.new_directory_edit_text);
        final ImageView createDir= (ImageView) findViewById(R.id.new_directory);
        createDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsFirstClick) {
                    newDirectory.setVisibility(View.VISIBLE);
                    createDir.setImageResource(R.drawable.ic_action_done);
                    newDirectory.requestFocus();
                    mIsFirstClick = false;
                } else {
                    String directoryName = newDirectory.getText().toString();
                    createDir.setImageResource(R.drawable.ic_add_folder);
                    newDirectory.setVisibility(View.GONE);
                    if (directoryName.length() > 0) {
                        new DropboxDirectoryCreatorAsyncTask(mApi, getApplicationContext(),
                                getCurrentPath() + "/" + directoryName, DropboxPickerActivity.this).execute();
                    }
                }
            }
        });
    }

    private void setUpBar(){
        TextView logoutTV = (TextView) findViewById(R.id.log_out_dropbox_label);
        logoutTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
                AppSharedPreferences.isDropBoxAuthenticated(getApplicationContext(), false);
                startActivity(new Intent(DropboxPickerActivity.this, AppAuthenticationActivity.class));
            }
        });

        ImageView save = (ImageView) findViewById(R.id.selection_directory);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppSharedPreferences.storeDropBoxUploadPath(getApplicationContext(), getCurrentPath());
                AppSharedPreferences.setPersonalNotesPreference(getApplicationContext(), AppConstant.DROP_BOX_SELECTION);
                showToast(AppConstant.IMAGE_LOCATION_SAVED_DROPBOX);
                actAsNote();
                startActivity(new Intent(DropboxPickerActivity.this, NotesActivity.class));
            }
        });

        ImageView back = (ImageView) findViewById(R.id.back_navigation);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initProgressDialog();
                try {
                    mDirectoryStack.pop();
                } catch (EmptyStackException e) {
                    startActivity(new Intent(DropboxPickerActivity.this, NotesActivity.class));
                }

                //Get the list of directories once you have moved one up
                new DropboxDirectoryListenerAsync(getApplicationContext(), mApi,
                        getCurrentPath(), DropboxPickerActivity.this).execute();
            }
        });
    }

    private void setUpList(){
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view_dropbox_directories);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getBaseContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        mDropboxAdapter = new DropboxAdapter(getApplicationContext(), new ArrayList<String>());
        recyclerView.setAdapter(mDropboxAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void OnItemClick(View view, int position) {
                TextView textView = (TextView) view.findViewById(R.id.dropbox_directory_name);
                String currentDirectory = textView.getText().toString();
                mDirectoryStack.push(currentDirectory);
                new DropboxDirectoryListenerAsync(getApplicationContext(), mApi,
                        getCurrentPath(), DropboxPickerActivity.this).execute();
            }

            @Override
            public void OnItemLongClick(View view, int position) {

            }
        }));
    }

    private String getCurrentPath(){
        String path = "";
        for(String p : mDirectoryStack){
            if(!p.equals("/")){
                path = path + "/" + p;
            }
        }
        return path;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void authenticate(){
        AndroidAuthSession session = DropboxActions.buildSession(getApplicationContext());
        mApi = new DropboxAPI<AndroidAuthSession>(session);
        mApi.getSession().startOAuth2Authentication(DropboxPickerActivity.this);
        mAfterAuth = true;
        AppSharedPreferences.setPersonalNotesPreference(getApplicationContext(), AppConstant.DROP_BOX_SELECTION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mAfterAuth) {
            AndroidAuthSession session = mApi.getSession();
            if(session.authenticationSuccessful()) {
                try {
                    session.finishAuthentication();
                    DropboxActions.storeAuth(session, getApplicationContext());
                    AppSharedPreferences.isDropBoxAuthenticated(getApplicationContext(), true);
                    initProgressDialog();
                    new DropboxDirectoryListenerAsync(getApplicationContext(),
                            mApi, getCurrentPath(), DropboxPickerActivity.this).execute();
                } catch (IllegalStateException e) {
                    showToast("Could not authenticate with dropbox " + e.getLocalizedMessage());
                }
            }
        }
    }

    private void logOut(){
        mApi.getSession().unlink();
        DropboxActions.clearKeys(getApplicationContext());
    }

    @Override
    public void onDirectoryCreateFinished(String dirName) {
        mDropboxAdapter.add(dirName);
        mDropboxAdapter.notifyDataSetChanged();
        TextView path = (TextView) findViewById(R.id.path_display);
        path.setText(getCurrentPath());
        mDialog.dismiss();
     }

    @Override
    public void onLoadFinished(List<String> values) {
        mDropboxAdapter.setData(values);
        mDropboxAdapter.notifyDataSetChanged();
        TextView path = (TextView) findViewById(R.id.path_display);
        path.setText(getCurrentPath());
        mDialog.dismiss();

    }

    private void initProgressDialog(){
        mDialog = new ProgressDialog(DropboxPickerActivity.this);
        mDialog.setTitle("Dropbox");
        mDialog.setMessage("Retrieving directories");
        mDialog.show();
    }
}
