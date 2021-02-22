package com.example.wetalkclient.constant;

import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final String SERVER_IP = "172.20.10.9";
    public static final int SERVER_PORT =8888;
    public static final String HTTP_SERVER_IP = "http://"+SERVER_IP+":8080";

    // 웹서버에 요청시 해당하는 코드
    public static class ID{
        public static final int CHECK_CODE = 201;
        public static final int REGISTER = 301;
        public static final int UPLOADIMAGE = 302;
        public static final int LOGIN = 401;
        public static final int MODIFY_USERINFO = 501;
        public static final int MODIFY_PWD = 601;
        public static final int GET_FRIEND_LIST = 701;
        public static final int SEARCH_USER = 801;
        public static final int REMOVE_FRIEND = 901;
    }
    // 회원가입 클래스
    public static class Register{
        public static final String IDCHECK_CODE_URL =HTTP_SERVER_IP+"/webServerProject/webServer/idCheck.do";
        public static final String REGISTER_URL = HTTP_SERVER_IP+"/webServerProject/webServer/join.do";
        public static final String UPLOADIMAGE_URL= HTTP_SERVER_IP+"/webServerProject/webServer/imageUpload.do";

        public static class RequestParams{
            public static final String USER_ID = "id";
            public static final String PASSWORD = "pwd";
            public static final String NIKNAME ="nikname";
            public static final String TIMESTAMP ="indate";
            public static final String PHOTO ="photo";
        }
        private static Map<String, String> errorMsg = new HashMap<>();
        static {
            errorMsg.put("201", "이미 가입된 메일주소 입니다.");
            errorMsg.put("202", "가입 실패하였습니다, 다시 시도해주세요!");
        }
        public static String GetErrorInfo(String code){
            return errorMsg.get(code);
        }
    }
    // 로그인 클래스
    public static class Login{
        public static final String LOGIN_URL = HTTP_SERVER_IP+"/webServerProject/webServer/login.do";

        public static class RequestParams{
            public static final String USER_ID = "id";
            public static final String PASSWORD = "pwd";
        }
        public static class ResponsParams{
            public static final String USER_ID = "id";
            public static final String NIKNAME ="nikname";
            public static final String PHOTO ="photo";
        }
        private static Map<String, String> errorMsg = new HashMap<>();
        static {
            errorMsg.put("301", "가입이 안된 메일주소 입니다.");
            errorMsg.put("302", "비밀번호가 일치하지 않습니다");
        }
        public static String GetErrorInfo(String code){
            return errorMsg.get(code);
        }
    }
    // 사용자 페이지 클래스
    public static class UserInfo{
        public static final String MODIFY_USERINFO_URL = HTTP_SERVER_IP+"/webServerProject/webServer/changeUserInfo.do";
        public static final String MODIFY_PWD_URL = HTTP_SERVER_IP+"/webServerProject/webServer/changePWD.do";
    }

    public static class RequestParams{
        public static final String USER_ID = "id";
        public static final String NIKNAME ="nikname";
        public static final String PHOTO ="photo";
        public static final String OLD_PWD ="old_pwd";
        public static final String PASSWORD = "pwd";

    }
    private static Map<String, String> errorMsg = new HashMap<>();
    static {
        errorMsg.put("401", "수정 실패하였습니다, 다시 시도해주세요!");
        errorMsg.put("402", "기존비번이 틀렸습니다~!!");
    }
    public static String GetErrorInfo(String code){
        return errorMsg.get(code);
    }
    // 친구리스트 GET
    public static class GetFriendList{
        public static final String MODIFY_USERINFO_URL = HTTP_SERVER_IP+"/webServerProject/webServer/getFriendList.do";
        public static class RequestParams{
            public static final String USER_ID = "id";
        }
    }
    // 친구 추가 클래스
    public static class AddFriend{
        public static final String SEARCH_USER_URL = HTTP_SERVER_IP+"/webServerProject/webServer/searchUser.do";
        public static class RequestParams{
            public static final String USER_ID = "id";
        }
        public static class ResponsParams{
            public static final String USER_ID = "id";
            public static final String NIKNAME ="nikname";
            public static final String PHOTO ="photo";
        }
        private static Map<String, String> errorMsg = new HashMap<>();
        static {
            errorMsg.put("501", "친추 실패하였습니다, 다시 시도해주세요!");
        }
        public static String GetErrorInfo(String code){
            return errorMsg.get(code);
        }
    }


}
