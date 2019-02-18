package com.ngbj.browser2.bean;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "WEATHER_SAVE_BEAN".
*/
public class WeatherSaveBeanDao extends AbstractDao<WeatherSaveBean, Long> {

    public static final String TABLENAME = "WEATHER_SAVE_BEAN";

    /**
     * Properties of entity WeatherSaveBean.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Temp = new Property(1, String.class, "temp", false, "TEMP");
        public final static Property Area = new Property(2, String.class, "area", false, "AREA");
        public final static Property Condition = new Property(3, String.class, "condition", false, "CONDITION");
    };


    public WeatherSaveBeanDao(DaoConfig config) {
        super(config);
    }
    
    public WeatherSaveBeanDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"WEATHER_SAVE_BEAN\" (" + //
                "\"_id\" INTEGER PRIMARY KEY ," + // 0: id
                "\"TEMP\" TEXT," + // 1: temp
                "\"AREA\" TEXT," + // 2: area
                "\"CONDITION\" TEXT);"); // 3: condition
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"WEATHER_SAVE_BEAN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, WeatherSaveBean entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String temp = entity.getTemp();
        if (temp != null) {
            stmt.bindString(2, temp);
        }
 
        String area = entity.getArea();
        if (area != null) {
            stmt.bindString(3, area);
        }
 
        String condition = entity.getCondition();
        if (condition != null) {
            stmt.bindString(4, condition);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, WeatherSaveBean entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String temp = entity.getTemp();
        if (temp != null) {
            stmt.bindString(2, temp);
        }
 
        String area = entity.getArea();
        if (area != null) {
            stmt.bindString(3, area);
        }
 
        String condition = entity.getCondition();
        if (condition != null) {
            stmt.bindString(4, condition);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    @Override
    public WeatherSaveBean readEntity(Cursor cursor, int offset) {
        WeatherSaveBean entity = new WeatherSaveBean( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // temp
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // area
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3) // condition
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, WeatherSaveBean entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setTemp(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setArea(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setCondition(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(WeatherSaveBean entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(WeatherSaveBean entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}
