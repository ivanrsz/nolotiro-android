/*
 * Copyright (c) 2013 "Pablo Castellano <pablo@anche.no>"
 * Copyright (c) 2013 "Eugenio Cano-Manuel Mendoza <eugeniocanom@gmail.com>"
 * Nolotiro App [http://nolotiro.org]
 *
 * This file is part of nolotiro-android.
 *
 * nolotiro-android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.alabs.nolotiro.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import org.alabs.nolotiro.Ad;
import org.alabs.nolotiro.Woeid;

import java.util.ArrayList;
import java.util.List;

// TODO: Cache woeid search
public class DbAdapter {

    // database
    private static final String DATABASE_NAME = "nolotiro";
    private static final String DATABASE_ADS_TABLE = "ads";
    private static final String DATABASE_WOEIDS_TABLE = "woeids";
    private static final int DATABASE_VERSION = 1;

    // ads table
    public static final String KEY_ID = BaseColumns._ID;
    public static final String KEY_TITLE = "title";
    public static final String KEY_BODY = "body";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_TYPE = "type";
    public static final String KEY_WOEID = "woeid";
    public static final String KEY_DATE = "date";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_STATUS = "status";
    public static final String KEY_FAVORITE = "favorite";

    // woeids table
    public static final String KEY_NAME = "name";
    public static final String KEY_ADMIN1 = "admin1";
    public static final String KEY_COUNTRY = "country";

    private static final String DATABASE_CREATE_ADS_TABLE = "create table " + DATABASE_ADS_TABLE + " (" +
            KEY_ID +" integer primary key, "+
            KEY_TITLE + " text not null, " +
            KEY_BODY + " text not null, " + // Nullable?
            KEY_USERNAME + " text not null, " +
            KEY_TYPE + " text not null, " +
            KEY_WOEID + " integer not null, " +
            KEY_DATE + " text not null, " +
            KEY_IMAGE + " text, " +
            KEY_STATUS + " text not null, " +
            KEY_FAVORITE + " boolean not null);";

    private static final String DATABASE_CREATE_WOEIDS_TABLE = "create table " + DATABASE_WOEIDS_TABLE + " (" +
            KEY_ID +" integer primary key, "+
            KEY_NAME + " text not null, " +
            KEY_ADMIN1 + " text not null, " + // Nullable?
            KEY_COUNTRY + " text not null);";

    private SQLiteHelper sqLiteHelper;
    private SQLiteDatabase sqLiteDatabase;
    private Context context;

    public DbAdapter(Context c){
        context = c;
    }

    public DbAdapter openToRead() throws android.database.SQLException {
        sqLiteHelper = new SQLiteHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        sqLiteDatabase = sqLiteHelper.getReadableDatabase();
        return this;
    }

    public DbAdapter openToWrite() throws android.database.SQLException {
        sqLiteHelper = new SQLiteHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
        sqLiteDatabase = sqLiteHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        sqLiteHelper.close();
    }

    public class SQLiteHelper extends SQLiteOpenHelper {
        public SQLiteHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE_ADS_TABLE);
            db.execSQL(DATABASE_CREATE_WOEIDS_TABLE);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_ADS_TABLE );
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_WOEIDS_TABLE );
            onCreate(db);
        }
    }

    // --------------- Ads table --------------------
    // TODO: Should update instead of insert when last_modified field is more recent
    public long insertAd(Ad ad) {
        ContentValues values = new ContentValues();
        values.put(KEY_ID, ad.getId());
        values.put(KEY_TITLE, ad.getTitle());
        values.put(KEY_BODY, ad.getBody());
        values.put(KEY_USERNAME, ad.getUsername());
        values.put(KEY_TYPE, String.valueOf(ad.getType()));
        values.put(KEY_WOEID, ad.getWoeid());
        values.put(KEY_DATE, ad.getDate());
        values.put(KEY_IMAGE, ad.getImageFilename());
        values.put(KEY_STATUS, String.valueOf(ad.getStatus()));
        values.put(KEY_FAVORITE, false);
        return sqLiteDatabase.insertWithOnConflict(DATABASE_ADS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public Ad getAd(int id) throws SQLException {
        Cursor mCursor =
                sqLiteDatabase.query(DATABASE_ADS_TABLE, new String[] {
                        KEY_ID,
                        KEY_TITLE,
                        KEY_BODY,
                        KEY_USERNAME,
                        KEY_TYPE,
                        KEY_WOEID,
                        KEY_DATE,
                        KEY_IMAGE,
                        KEY_STATUS,
                        KEY_FAVORITE
                },
                        KEY_ID + "= '" + id + "'",
                        null,
                        //KEY_ID + "= '?s'",
                        //new String[] { Integer.toString(id) },
                        null,
                        null,
                        null);
        if (mCursor != null && mCursor.getCount() > 0) {
            mCursor.moveToFirst();
            Ad ad = new Ad();
            ad.setId(mCursor.getInt(mCursor.getColumnIndex(KEY_ID)));
            ad.setTitle(mCursor.getString(mCursor.getColumnIndex(KEY_TITLE)));
            ad.setBody(mCursor.getString(mCursor.getColumnIndex(KEY_BODY)));
            ad.setUsername(mCursor.getString(mCursor.getColumnIndex(KEY_USERNAME)));
            ad.setType(Ad.Type.valueOf(mCursor.getString(mCursor.getColumnIndex(KEY_TYPE))));
            ad.setWoeid(mCursor.getInt(mCursor.getColumnIndex(KEY_WOEID)));
            ad.setDate(mCursor.getString(mCursor.getColumnIndex(KEY_DATE)));
            ad.setImageFilename(mCursor.getString(mCursor.getColumnIndex(KEY_IMAGE)));
            ad.setStatus(Ad.Status.valueOf(mCursor.getString(mCursor.getColumnIndex(KEY_STATUS))));

            if (mCursor.getInt(mCursor.getColumnIndex(KEY_FAVORITE)) == 1)
                ad.setFavorite(true);
            else
                ad.setFavorite(false);

            return ad;
        }
        return null;
    }

    public List<Ad> getFavoriteAds(Ad.Type type, int limit) throws SQLException {
        List<Ad> ads = null;

        Cursor mCursor =
                sqLiteDatabase.query(DATABASE_ADS_TABLE, new String[] {
                        KEY_ID,
                        KEY_TITLE,
                        KEY_BODY,
                        KEY_USERNAME,
                        KEY_TYPE,
                        KEY_WOEID,
                        KEY_DATE,
                        KEY_IMAGE,
                        KEY_STATUS
                },
                        //KEY_ID + "= '" + id + "'",
                        //null,
                        KEY_FAVORITE + "= true AND " + KEY_TYPE + "= '?s'",
                        new String[] { String.valueOf(type) },
                        null,
                        null,
                        null); //TODO: set limit
        if (mCursor != null && mCursor.getCount() > 0) {
            ads = new ArrayList<Ad>();
            mCursor.moveToFirst();
            while(mCursor != null) {
                Ad ad = new Ad();
                ad.setId(mCursor.getInt(mCursor.getColumnIndex(KEY_ID)));
                ad.setTitle(mCursor.getString(mCursor.getColumnIndex(KEY_TITLE)));
                ad.setBody(mCursor.getString(mCursor.getColumnIndex(KEY_BODY)));
                ad.setUsername(mCursor.getString(mCursor.getColumnIndex(KEY_USERNAME)));
                ad.setType(Ad.Type.valueOf(mCursor.getString(mCursor.getColumnIndex(KEY_TYPE))));
                ad.setWoeid(mCursor.getInt(mCursor.getColumnIndex(KEY_WOEID)));
                ad.setDate(mCursor.getString(mCursor.getColumnIndex(KEY_DATE)));
                ad.setImageFilename(mCursor.getString(mCursor.getColumnIndex(KEY_IMAGE)));
                ad.setStatus(Ad.Status.valueOf(mCursor.getString(mCursor.getColumnIndex(KEY_STATUS))));
                ads.add(ad);
                mCursor.moveToNext();
            }
        }
        return ads;
    }


    public boolean isAdFavorite(int id) throws SQLException {
        Cursor mCursor =
                sqLiteDatabase.query(DATABASE_ADS_TABLE, new String[] {
                        KEY_FAVORITE
                },
                        KEY_ID + "= '" + id + "'",
                        null,
                        //KEY_ID + "= '?s'",
                        //new String[] { Integer.toString(id) },
                        null,
                        null,
                        null);
        if (mCursor != null && mCursor.getCount() > 0) {
            mCursor.moveToFirst();
            return mCursor.getInt(mCursor.getColumnIndex(KEY_FAVORITE)) != 0;
        }
        return false;
    }

    public boolean markAdAsFavorite(int id, boolean fav) {
        ContentValues args = new ContentValues();
        args.put(KEY_FAVORITE, fav);
        return sqLiteDatabase.update(DATABASE_ADS_TABLE, args, KEY_ID + "='" + id + "'", null) > 0;
    }

    // TODO
    public List<Ad> getAdsFromWoeid(int woeid) {
        return null;
    }

    // --------------- Woeids table --------------------
    public long insertWoeid(Woeid woeid) {
        ContentValues values = new ContentValues();
        values.put(KEY_ID, woeid.getId());
        values.put(KEY_NAME, woeid.getName());
        values.put(KEY_ADMIN1, woeid.getAdmin());
        values.put(KEY_COUNTRY, woeid.getCountry());
        return sqLiteDatabase.insertWithOnConflict(DATABASE_WOEIDS_TABLE, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public Woeid getWoeid(int id) throws SQLException {
        Cursor mCursor =
                sqLiteDatabase.query(true, DATABASE_WOEIDS_TABLE, new String[] {
                        KEY_ID,
                        KEY_NAME,
                        KEY_ADMIN1,
                        KEY_COUNTRY
                },
                        KEY_ID + "= '" + id + "'",
                        null,
                        null,
                        null,
                        null,
                        null);
        if (mCursor != null && mCursor.getCount() > 0) {
            mCursor.moveToFirst();

            int wid = mCursor.getInt(mCursor.getColumnIndex(KEY_ID));
            String name = mCursor.getString(mCursor.getColumnIndex(KEY_NAME));
            String admin = mCursor.getString(mCursor.getColumnIndex(KEY_ADMIN1));
            String country = mCursor.getString(mCursor.getColumnIndex(KEY_COUNTRY));

            return new Woeid(wid, name, admin, country);
        }
        return null;
    }


    public List<Woeid> getWoeids() {
        List<Woeid> woeids = null;

        Cursor mCursor =
                sqLiteDatabase.query(true, DATABASE_WOEIDS_TABLE, new String[] {
                        KEY_ID,
                        KEY_NAME,
                        KEY_ADMIN1,
                        KEY_COUNTRY
                },
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);
        if (mCursor != null && mCursor.getCount() > 0) {
            woeids = new ArrayList<Woeid>();
            int wid;
            String name, admin, country;

            mCursor.moveToFirst();
            do {
                wid = mCursor.getInt(mCursor.getColumnIndex(KEY_ID));
                name = mCursor.getString(mCursor.getColumnIndex(KEY_NAME));
                admin = mCursor.getString(mCursor.getColumnIndex(KEY_ADMIN1));
                country = mCursor.getString(mCursor.getColumnIndex(KEY_COUNTRY));
                woeids.add(new Woeid(wid, name, admin, country));
            } while(mCursor.moveToNext());
        }
        return woeids;
    }
}