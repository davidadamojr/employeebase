package com.davidadamojr.employeebase;

/**
 * Created by davidadamojr on 2/19/17.
 */
public class Config {
    public static final String BASE_URL = "http://cliptext.co/employeebase/";
    public static final String URL_ADD = BASE_URL + "add_employee.php";
    public static final String URL_GET_ALL = BASE_URL + "all_employees.ARRAYphp";
    public static final String URL_GET_EMP = BASE_URL + "get_employee.php?id=";
    public static final String URL_UPDATE_EMP = BASE_URL + "update_employee.php";
    public static final String URL_DELETE_EMP = BASE_URL + "delete_employee.php?id=";

    public static final String KEY_EMP_ID = "id";
    public static final String KEY_EMP_FNAME = "fname";
    public static final String KEY_EMP_LNAME = "lname";
    public static final String KEY_EMP_TITLE = "title";
    public static final String KEY_EMP_SAL = "salary";

    public static final String TAG_JSON_ARRAY = "result";
    public static final String TAG_ID = "id";
    public static final String TAG_FNAME = "fname";
    public static final String TAG_LNAME = "lname";
    public static final String TAG_NAME = "name";
    public static final String TAG_TITLE = "title";
    public static final String TAG_SAL = "salary";

    public static final String EMP_ID = "emp_id";
}
