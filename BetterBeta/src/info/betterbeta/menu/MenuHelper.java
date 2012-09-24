package info.betterbeta.menu;

import info.betterbeta.R;
import info.betterbeta.area.AreaHelper;
import info.betterbeta.media.MediaHelper;
import info.betterbeta.model.Area;
import info.betterbeta.model.IdType;
import info.betterbeta.model.Media;
import info.betterbeta.model.Permission;
import info.betterbeta.model.Problem;
import info.betterbeta.problem.ProblemHelper;
import android.content.ContentResolver;
import android.view.Menu;

public class MenuHelper {
	public static final int NEW = 1;
	public static final int EDIT = 2;
	public static final int DELETE = 3;
	public static final int MAP = 4;
	public static final int MAIN_MENU = 5;
	public static final int VIEW = 6;
	public static final int SYNC = 7;
	public static final int DISCARD = 8;
	public static final int CREATE = 9;
	public static final int ADD_MEDIA = 10;
	public static final int NEW_PROBLEM = 11;
	public static final int CANCEL_EDIT = 12;
	public static final int TOGGLE_LOCATION = 13;
	public static final int USE = 14;
	public static final int TOGGLE_PROBLEMS = 15;
	public static final int ME = 16;
	public static final int PROBLEM = 17;
	public static final int AREA = 18;
	public static final int ADD_PROBLEM = 19;
	public static final int FOLLOW_ME = 20;
	public static final int PICK_IMAGE = 21;
	public static final int NEW_PROBLEM_NEAR = 22;
	public static final int MAP_PROBLEM = 23;
	public static final int VIEW_PROBLEM = 24;
	public static final int EDIT_PROBLEM = 25;
	public static final int DELETE_PROBLEM = 26;
	public static final int VIEW_MEDIA = 27;
	public static final int EDIT_MEDIA = 28;
	public static final int DELETE_MEDIA = 29;
	public static final int ADD_MEDIA_TO_PROBLEM = 30;
	
	public static void createProblemsMenu(Menu menu, boolean isSyncing, long selectedId, ContentResolver cr){
		if(selectedId > 0){ // problem is selected
			Problem problem = ProblemHelper.inflatePermission(selectedId, cr, IdType.LOCAL);
			menu.add(Menu.NONE, MenuHelper.MAP, 0, R.string.map);
			menu.add(Menu.NONE, MenuHelper.VIEW, 0, R.string.view);
			if(!isSyncing){
				menu.add(Menu.NONE, MenuHelper.ADD_MEDIA, 0, R.string.add_media);
				if(problem.getPermission() == Permission.GOD){
					menu.add(Menu.NONE, MenuHelper.EDIT, 0, R.string.edit);
					menu.add(Menu.NONE, MenuHelper.DELETE, 0, R.string.delete);
				}
			}
		}
		else{ 
			if(!isSyncing){
				menu.add(Menu.NONE, MenuHelper.NEW, 0, R.string.new_problem);
			}
		}
		menu.add(Menu.NONE, MenuHelper.MAIN_MENU, 0, R.string.main_menu);
	}

	public static void createAreasMenu(Menu menu, boolean isSyncing, long selectedId, ContentResolver cr){
		if(selectedId > 0){ // problem is selected
			Area area = AreaHelper.inflatePermission(selectedId, cr, IdType.LOCAL);
			menu.add(Menu.NONE, MenuHelper.MAP, 0, R.string.map);
			menu.add(Menu.NONE, MenuHelper.VIEW, 0, R.string.view);
			if(!isSyncing){
				menu.add(Menu.NONE, MenuHelper.NEW_PROBLEM, 0, R.string.new_problem);
				if(area.getPermission() == Permission.GOD){
					menu.add(Menu.NONE, MenuHelper.EDIT, 0, R.string.edit);
					menu.add(Menu.NONE, MenuHelper.DELETE, 0, R.string.delete);
				}
			}
		}
		else{ 
			if(!isSyncing){
				menu.add(Menu.NONE, MenuHelper.NEW, 0, R.string.new_area);
			}
		}
		menu.add(Menu.NONE, MenuHelper.MAIN_MENU, 0, R.string.main_menu);
	}
	
	public static void createProblemDetailMenu(Menu menu, boolean isSyncing, long selectedId, ContentResolver cr, boolean mediaLongClicked){
		if(mediaLongClicked){ // problem is selected
			Media media = MediaHelper.inflatePermission(selectedId, cr, IdType.LOCAL);
			menu.add(Menu.NONE, MenuHelper.VIEW_MEDIA, 0, R.string.view);
			if(!isSyncing){
				if(media.getPermission() == Permission.GOD){
					menu.add(Menu.NONE, MenuHelper.EDIT_MEDIA, 0, R.string.edit);
					menu.add(Menu.NONE, MenuHelper.DELETE_MEDIA, 0, R.string.delete);
				}
			}
		}
		else{
			Problem problem = ProblemHelper.inflatePermission(selectedId, cr, IdType.LOCAL);
			menu.add(Menu.NONE, MenuHelper.MAP, 0, R.string.map);
			if(!isSyncing){
				menu.add(Menu.NONE, MenuHelper.NEW_PROBLEM_NEAR, 0, R.string.new_problem_near);
				menu.add(Menu.NONE, MenuHelper.ADD_MEDIA, 0, R.string.add_media);
				if(problem.getPermission() == Permission.GOD){
					menu.add(Menu.NONE, MenuHelper.EDIT, 0, R.string.edit);
					menu.add(Menu.NONE, MenuHelper.DELETE, 0, R.string.delete);
				}
			}
		}
		menu.add(Menu.NONE, MenuHelper.MAIN_MENU, 0, R.string.main_menu);
	}
	public static void createAreaDetailMenu(Menu menu, boolean isSyncing, long selectedId, ContentResolver cr, boolean problemLongClicked){
		
		if(problemLongClicked){ // problem is selected
			Problem problem = ProblemHelper.inflatePermission(selectedId, cr, IdType.LOCAL);
			menu.add(Menu.NONE, MenuHelper.MAP_PROBLEM, 0, R.string.map);
			menu.add(Menu.NONE, MenuHelper.VIEW_PROBLEM, 0, R.string.view);
			if(!isSyncing){
				menu.add(Menu.NONE, MenuHelper.ADD_MEDIA_TO_PROBLEM, 0, R.string.add_media);
				if(problem.getPermission() == Permission.GOD){
					menu.add(Menu.NONE, MenuHelper.EDIT_PROBLEM, 0, R.string.edit);
					menu.add(Menu.NONE, MenuHelper.DELETE_PROBLEM, 0, R.string.delete);
				}
			}
		}
		else{
			Area area = AreaHelper.inflatePermission(selectedId, cr, IdType.LOCAL);
			menu.add(Menu.NONE, MenuHelper.MAP, 0, R.string.map);
			if(!isSyncing){
				menu.add(Menu.NONE, MenuHelper.NEW_PROBLEM, 0, R.string.new_problem);
				if(area.getPermission() == Permission.GOD){
					menu.add(Menu.NONE, MenuHelper.EDIT, 0, R.string.edit);
					menu.add(Menu.NONE, MenuHelper.DELETE, 0, R.string.delete);
				}
			}
		}
		menu.add(Menu.NONE, MenuHelper.MAIN_MENU, 0, R.string.main_menu);
		
	}
}
