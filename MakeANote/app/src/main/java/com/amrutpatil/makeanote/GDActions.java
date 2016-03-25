package com.amrutpatil.makeanote;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filter;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Amrut on 3/7/16.
 */
final class GDActions {
    private GDActions() {
    }

    private static GoogleApiClient mGAC;
    private static com.google.api.services.drive.Drive mGOOSvc;

    static void init(NoteDetailActivity ctx, String email) {
        if (ctx != null && email != null && mGAC == null) {
            mGAC = new GoogleApiClient.Builder(ctx)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .setAccountName(email)
                    .addConnectionCallbacks(ctx).addOnConnectionFailedListener(ctx).build();

            mGOOSvc = new com.google.api.services.drive.Drive.Builder(
                    AndroidHttp.newCompatibleTransport(), new GsonFactory(), GoogleAccountCredential
                    .usingOAuth2(ctx, Arrays.asList(DriveScopes.DRIVE_FILE))
                    .setSelectedAccountName(email)
            ).build();
        }
    }

    static void init(NotesActivity ctx, String email) {
        if (ctx != null && email != null) {
            mGAC = new GoogleApiClient.Builder(ctx).addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE).setAccountName(email)
                    .addConnectionCallbacks(ctx).addOnConnectionFailedListener(ctx).build();

            mGOOSvc = new com.google.api.services.drive.Drive.Builder(
                    AndroidHttp.newCompatibleTransport(), new GsonFactory(), GoogleAccountCredential
                    .usingOAuth2(ctx, Arrays.asList(DriveScopes.DRIVE_FILE))
                    .setSelectedAccountName(email)
            ).build();
        }
    }
    static void connect(boolean bOn) {
        if (mGAC != null) {
            if (bOn) {
                if (!mGAC.isConnected()) {
                    mGAC.connect();
                }
            } else {
                if (mGAC.isConnected()) {
                    mGAC.disconnect();
                }
            }
        }
    }

    private static boolean isConnected() {
        return mGAC != null && mGAC.isConnected();
    }

    static ArrayList<GF> search(DriveId prId, String titl, String mime) {
        ArrayList<GF> gfs = new ArrayList<>();

        if (isConnected()) {
            ArrayList<Filter> fltrs = new ArrayList<>();
            if (prId != null) fltrs.add(Filters.in(SearchableField.PARENTS, prId));
            if (titl != null) fltrs.add(Filters.eq(SearchableField.TITLE, titl));
            if (mime != null) fltrs.add(Filters.eq(SearchableField.MIME_TYPE, mime));
            Query qry = new Query.Builder().addFilter(Filters.and(fltrs)).build();
            DriveApi.MetadataBufferResult rslt = Drive.DriveApi.query(mGAC, qry).await();
            if (rslt.getStatus().isSuccess()) {
                MetadataBuffer mdb = null;
                try {
                    mdb = rslt.getMetadataBuffer();
                    for (Metadata md : mdb) {
                        if (md == null || !md.isDataValid() || md.isTrashed()) continue;
                        gfs.add(new GF(md.getTitle(), md.getDriveId().encodeToString()));
                    }
                } finally {
                    if (mdb != null) mdb.release();
                }
            }
        }
        return gfs;
    }
    static DriveId create(DriveId prId, String titl, String mime, byte[] buf) {
        if (titl == null || !isConnected()) return null;
        DriveId dId = null;
        DriveFolder pFldr = (prId == null) ?
                Drive.DriveApi.getRootFolder(mGAC) : Drive.DriveApi.getFolder(mGAC, prId);
        if (pFldr == null) return null;

        MetadataChangeSet meta;
        if (buf != null) {
            if (mime != null) {
                DriveApi.DriveContentsResult r1 = Drive.DriveApi.newDriveContents(mGAC).await();
                if (r1 == null || !r1.getStatus().isSuccess()) return null;

                meta = new MetadataChangeSet.Builder().setTitle(titl).setMimeType(mime).build();
                DriveFolder.DriveFileResult r2 = pFldr.createFile(mGAC, meta, r1.getDriveContents()).await();
                DriveFile dFil = r2 != null && r2.getStatus().isSuccess() ? r2.getDriveFile() : null;
                if (dFil == null) return null;

                r1 = dFil.open(mGAC, DriveFile.MODE_WRITE_ONLY, null).await();
                if ((r1 != null) && (r1.getStatus().isSuccess())) try {
                    Status stts = bytes2Cont(r1.getDriveContents(), buf).commit(mGAC, meta).await();
                    if ((stts != null) && stts.isSuccess()) {
                        DriveResource.MetadataResult r3 = dFil.getMetadata(mGAC).await();
                        if (r3 != null && r3.getStatus().isSuccess()) {
                            dId = r3.getMetadata().getDriveId();
                        }
                    }
                } catch (Exception e) {
                    GDUT.le(e);
                }
            }

        } else {
            meta = new MetadataChangeSet.Builder().setTitle(titl).setMimeType(GDUT.MIME_FLDR).build();
            DriveFolder.DriveFolderResult r1 = pFldr.createFolder(mGAC, meta).await();
            DriveFolder dFld = (r1 != null) && r1.getStatus().isSuccess() ? r1.getDriveFolder() : null;
            if (dFld != null) {
                DriveResource.MetadataResult r2 = dFld.getMetadata(mGAC).await();
                if ((r2 != null) && r2.getStatus().isSuccess()) {
                    dId = r2.getMetadata().getDriveId();
                }
            }
        }
        return dId;
    }
    static DriveId createTreeGDAA(String root, String titl, byte[] buf) {
        if (root == null || titl == null) return null;
        DriveId dId = findOrCreateFolder((DriveId) null, root);
        if (dId != null) {
            dId = findOrCreateFolder(dId, GDUT.titl2Month(titl));
            if (dId != null) {
                return create(dId, titl + GDUT.JPEG_EXT, GDUT.MIME_JPEG, buf);
            }
        }
        return null;
    }


    private static DriveId findOrCreateFolder(DriveId prnt, String titl) {
        ArrayList<GF> gfs = search(prnt, titl, GDUT.MIME_FLDR);
        if (gfs.size() > 0) {
            return DriveId.decodeFromString(gfs.get(0).id);
        }
        return create(prnt, titl, null, null);
    }


    private static DriveContents bytes2Cont(DriveContents driveContents, byte[] buf) {
        OutputStream os = driveContents.getOutputStream();
        try {
            os.write(buf);
        } catch (IOException e) {
            GDUT.le(e);
        } finally {
            try {
                os.close();
            } catch (Exception e) {
                GDUT.le(e);
            }
        }
        return driveContents;
    }

    static ArrayList<GF> search(String prId, String titl, String mime) {
        ArrayList<GF> gfs = new ArrayList<>();
        String qryClause = "'me' in owners and ";
        if (prId != null) qryClause += "'" + prId + "' in parents and ";
        if (titl != null) qryClause += "title = '" + titl + "' and ";
        if (mime != null) qryClause += "mimeType = '" + mime + "' and ";
        qryClause = qryClause.substring(0, qryClause.length() - " and ".length());
        com.google.api.services.drive.Drive.Files.List qry = null;
        try {
            qry = mGOOSvc.files().list().setQ(qryClause)
                    .setFields("items(id, labels/trashed, title), nextPageToken");
            gfs = search(gfs, qry);
        } catch (GoogleAuthIOException gaiEx) {
            try {
                gfs = search(gfs, qry);
            } catch (Exception g) {
                GDUT.le(g);
            }
        } catch (Exception e) {
            GDUT.le(e);
        }
        return gfs;
    }

    static String create(String prId, String titl, String mime, byte[] buf) {
        String rsid = null;
        if (titl != null && isConnected()) {

            File meta = new File();
            meta.setParents(Arrays.asList(new ParentReference().setId(prId == null ? "root" : prId)));
            meta.setTitle(titl);

            File gFl = null;
            if (buf != null) {
                if (mime != null) {
                    meta.setMimeType(mime);
                    java.io.File jvFl;
                    try {
                        jvFl = GDUT.bytes2File(buf,
                                java.io.File.createTempFile(GDUT.TMP_FILENM, null, GDUT.acx.getCacheDir()));
                        gFl = mGOOSvc.files().insert(meta, new FileContent(mime, jvFl)).execute();
                    } catch (Exception e) {
                        GDUT.le(e);
                    }
                    if (gFl != null && gFl.getId() != null) {
                        rsid = gFl.getId();
                    }
                }
            } else {
                meta.setMimeType(GDUT.MIME_FLDR);
                try {
                    gFl = mGOOSvc.files().insert(meta).execute();
                } catch (Exception e) {
                    GDUT.le(e);
                }
                if (gFl != null && gFl.getId() != null) {
                    rsid = gFl.getId();
                }
            }
        }
        return rsid;
    }
    static byte[] read(String rsId, int thmbSz) {
        byte[] buf = null;
        if (isConnected() && rsId != null) try {
            File gFl = (thmbSz == 0) ?
                    mGOOSvc.files().get(rsId).setFields("downloadUrl").execute() :
                    mGOOSvc.files().get(rsId).setFields("thumbnailLink").execute();
            if (gFl != null) {
                String strUrl;
                if (thmbSz == 0)
                    strUrl = gFl.getDownloadUrl();
                else {
                    strUrl = gFl.getThumbnailLink();
                    if (!strUrl.endsWith("s220")) return null;
                    strUrl = strUrl.substring(0, strUrl.length() - 3) + Integer.toString(thmbSz);
                }
                InputStream is = mGOOSvc.getRequestFactory()
                        .buildGetRequest(new GenericUrl(strUrl)).execute().getContent();
                buf = GDUT.is2Bytes(is);
            }
        } catch (Exception e) {
            GDUT.le(e);
        }
        return buf;
    }

    private static ArrayList<GF> search(ArrayList<GF> gfs, com.google.api.services.drive.Drive.Files.List qry) throws IOException {
        String npTok = null;
        if (qry != null) do {
            FileList gLst = qry.execute();
            if (gLst != null) {
                for (File gFl : gLst.getItems()) {
                    if (gFl.getLabels().getTrashed()) continue;
                    gfs.add(new GF(gFl.getTitle(), gFl.getId()));
                }
                npTok = gLst.getNextPageToken();
                qry.setPageToken(npTok);
            }
        } while (npTok != null && npTok.length() > 0);
        return gfs;
    }

    final static class GF {
        String titl, id;

        GF(String t, String i) {
            titl = t;
            id = i;
        }
    }
}
