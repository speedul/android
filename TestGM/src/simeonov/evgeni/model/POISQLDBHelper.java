package simeonov.evgeni.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class POISQLDBHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "pois.db";
	private static final int DATABASE_VERSION = 1;
	
	public POISQLDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "CREATE TABLE [points_of_interest] ( "
				+ "[id] INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ "[title] VARCHAR(250), " 
				+ "[details] TEXT, " 
				+ "[longitude] FLOAT, "
				+ "[latitude] FLOAT);";
		db.execSQL(sql);

		db.execSQL("INSERT INTO points_of_interest (id, title, details, longitude, latitude) values (1, 'Point of Interest 1', 'test 1', 43.25082158319279, 26.56966209411621)");
		db.execSQL("INSERT INTO points_of_interest (id, title, details, longitude, latitude) values (2, 'Point of Interest 2', 'test 2', 43.2500713897105, 26.572494506835938)");
		db.execSQL("INSERT INTO points_of_interest (id, title, details, longitude, latitude) values (3, 'Point of Interest 3', '', 43.25088409889921, 26.574339866638184)");
		db.execSQL("INSERT INTO points_of_interest (id, title, details, longitude, latitude) values (4, 'Point of Interest 4', '', 43.24888356446593, 26.574296951293945)");
		db.execSQL("INSERT INTO points_of_interest (id, title, details, longitude, latitude) values (5, 'Point of Interest 5', '', 43.24769571605657, 26.572237014770508)");
		db.execSQL("INSERT INTO points_of_interest (id, title, details, longitude, latitude) values (6, 'Point of Interest 6', '', 43.24888356446593, 26.569790840148926)");
		db.execSQL("INSERT INTO points_of_interest (id, title, details, longitude, latitude) values (7, 'Point of Interest 7', '', 43.25338467453802, 26.57266616821289)");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion >= newVersion) {
			return;
		}
	}

}
