package simar.travelentapp.HelperClasses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by simar on 4/11/18.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String _tableNameFavorites = "FAVORITES";
    private static final String _tableNameSearchResults = "SEARCH_RESULTS";
    private static final String _colPlaceId = "PLACE_ID";
    private static final String _colPlaceName = "PLACE_NAME";
    private static final String _colPlaceLocation = "PLACE_LOCATION";
    private static final String _colPlaceIcon = "PLACE_ICON";

    public DatabaseHelper(Context context) {
        super(context, "TRAVEL_ENT", null, 1);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE FAVORITES (ROWID INTEGER PRIMARY KEY, PLACE_ID STRING, PLACE_NAME TEXT, PLACE_LOCATION TEXT, PLACE_ICON TEXT)";
        db.execSQL(createTable);

        createTable = "CREATE TABLE SEARCH_RESULTS (ROWID INTEGER PRIMARY KEY, PLACE_ID, PLACE_NAME TEXT, PLACE_LOCATION TEXT, PLACE_ICON TEXT)";
        db.execSQL(createTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropTable = "DROP IF TABLE EXISTS FAVORITES";
        db.execSQL(dropTable);

        dropTable = "DROP IF TABLE EXISTS SEARCH_RESULTS";
        db.execSQL(dropTable);

        onCreate(db);
    }

    public boolean addData(String placeID, String placeName, String placeLocation, String placeImage, String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(_colPlaceId, placeID);
        contentValues.put(_colPlaceIcon, placeImage);
        contentValues.put(_colPlaceName, placeName);
        contentValues.put(_colPlaceLocation, placeLocation);

        long insert = db.insert(tableName, null, contentValues);
        if (insert == -1) {
            return false;
        } else {
            return true;
        }
    }

    public boolean deleteData(String placeID, String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(tableName, _colPlaceId + " = ?", new String[]{placeID}) > 0;
    }

    public ArrayList<Places> getFavoriteData() {
        ArrayList<Places> placeList = new ArrayList<>();

        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM FAVORITES";
        Cursor data = db.rawQuery(selectQuery, null);

        if(data.getCount() > 0) {
            while(data.moveToNext()){
                String placeID = data.getString(1);
                String placeName = data.getString(2);
                String placeLocation = data.getString(3);
                String placeIcon = data.getString(4);

                Places place = new Places(placeID, placeName, placeLocation, placeIcon);
                placeList.add(place);
            }
        }
        return placeList;
    }

    public Cursor getSearchResultsData() {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM SEARCH_RESULTS";
        Cursor data = db.rawQuery(selectQuery, null);
        return data;
    }

    public Cursor getFavoriteData(int start, int end) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM FAVORITES WHERE ROWID >= " + start + " AND ROWID <= " + end;
        Cursor data = db.rawQuery(selectQuery, null);
        return data;
    }

    public Cursor getSearchResultsData(int start, int end) {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM SEARCH_RESULTS WHERE ROWID >= " + start + " AND ROWID <= " + end;
        Cursor data = db.rawQuery(selectQuery, null);
        return data;
    }

    public void deleteSearchResultsData() {
        SQLiteDatabase db = this.getWritableDatabase();
        String delData = "DELETE FROM SEARCH_RESULTS";
        db.execSQL(delData);
    }

    public ArrayList<String> getFavoritePlaceIdList() {
        ArrayList<String> placeIdList = new ArrayList<>();

        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT PLACE_ID FROM FAVORITES";
        Cursor data = db.rawQuery(selectQuery, null);

        if (data.getCount() > 0) {
            while (data.moveToNext()) {
                placeIdList.add(data.getString(0));
            }
        }

        return placeIdList;
    }
}
