package com.delivery.utils.jkeys;

public interface Keys {
    public interface Log {
        public static String LOG_TAG = "OMM_LOGS";
    }

    public interface CommonResources {

        public static final String CONNECTION_URL = "https://www.krenai.online/";
        public static final String CONNECTION_URL_DEMAND = "https://www.outsourcecto.com/";


//        public static final String CONNECTION_URL = "http://ec2-3-6-120-2.ap-south-1.compute.amazonaws.com/";
//        public static final String CONNECTION_URL_DEMAND = "http://ec2-3-7-159-160.ap-south-1.compute.amazonaws.com/";
        public static final String CONNECTION_URL_ORG = "http://org-manager-dot-dev-odmm.appspot.com/";
        public static final String CONNECTION_URL_CAMPAIGN = "http://campaign-manager-dot-dev-odmm.appspot.com/";
        public static final String CONNECTION_URL_SITE_MANAGER = "http://site-manager-dot-dev-odmm.appspot.com/";
        public static final String CONNECTION_URL_RFP_MANAGER = "http://rfp-manager-dot-dev-odmm.appspot.com/";
        public static final String FIREBASE_URL = "https://firebasestorage.googleapis.com";
        public static final String FIREBASE_BUCKET = "gs://godigilab-a47c7.appspot.com/";
        public static final int DEFAULT_ZOOM = 16;
        public static int ACTIVITY_TIME_OUT = 1500;
        public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
        public static final String REQUEST_TYPE = "WEB";
        public static final int AUTOCOMPLETE_REQUEST_CODE = 1;
        public static final int STATIC_SUPPLIER_ID = 59;
        public static final String PLACES_API_KEY = "AIzaSyDfa_w2o59Y-ODNukuKeHk6MVgLWzO3hHs";
    }

    //LoginActivity Resources - REST API and Request/Response attributes
    public interface LoginKeys {
        //LoginActivity REST API
        public static final String DATA_PATH = "api/v4/delivery";
        public static final String DATA_PATH_OTP_VERIFY = "api/v4/delivery/verify";
        public static final String DATA_PATH_OTP_RESEND = "api/v1/crowd/users/request/otp";
        public static final String DATA_PATH_DOC_UPLOAD = "api/v4/delivery";
        public static final String DATA_PATH_LOGIN = "oauth/token";
        public static final String DATA_PATH_TOKEN = "rest/api/refresh/token";
        //Request & response attributes
        public static final String USER_EMAIL = "emailAddress";
        public static final String USER_FIRST_NAME = "firstName";
        public static final String USER_LAST_NAME = "lastName";
        public static final String USER_ID = "userId";
        public static final String USER_PHONE_NUMBER = "phoneNo";
        public static final String USER_NAME = "username";
        public static final String USER_SAFE_CODE = "password";
        public static final String USER_PASSWORD = "password";
        public static final String USER_ADDRESS_STREET = "addressStreet";
        public static final String USER_ADDRESS_LINE_TWO = "addressLine2";
        public static final String USER_ADDRESS_CITY = "addressCity";
        public static final String USER_ADDRESS_STATE = "addressState";
        public static final String USER_ADDRESS_PIN_CODE = "addressPin";
        public static final String SUPPLIER_ID = "supplierId";
        public static final String USER_PROFILE_IMAGE_URL = "profilePicUrl";
        public static final String USER_LICENCE_IMAGE_URL = "licenceCard";
        public static final String USER_ADHAR_IMAGE_URL = "aadharCard";
        public static final String STATUS = "status";
        public static final String ONE_TIME_PASSWORD = "otpNum";
        public static final String TOKEN = "access_token";
        public static final String FIREBASE_TOKEN = "idToken";
        public static final String FIRE_BASE_API = "deviceToken";
    }

    public interface Setting {
        //Profile Details REST API
        public static String DATA_PATH_USER_DETAILS = "api/v3/account/employee/employee/";
        //Request & response attributes
        public static String USER_PHOTO_IMAGE_URL = "photoImageUrl";
        public static String EMAIL_ADDRESS = "emailId";
        public static String MOBILE_NUMBER = "contactNo";
        public static String ADDRESS = "defaultUserAddressBook";
        public static String GOOGLE_ADDRESS = "googleAddress";
        public static String CUSTOM_ADDRESS = "customAddress";
    }

    //Forgot Password REST API and Request/Response Attributes
    public interface ForgotPassword {
        public static final String DATA_PATH_VERIFY_PHONE = "api/v3/account/employee/forget/otp/59/";
        public static final String DATA_PATH_UPDATE_SAFE_CODE = "api/v3/account/employee/verify/forget/otp/59/";
    }

    //Background Location Update
    public interface RefreshLocation {
        public static final String DATA_PATH_REFRESH_LOCATION = "api/v4/delivery/location/update";
        public static final String LOCATION_UPDATE_LATITUDE = "latitude";
        public static final String LOCATION_UPDATE_LONGITUDE = "longitude";
        public static final String LOCATION_UPDATE_TIME = "locationTime";
    }

    //Background Location Update
    public interface FireBaseToken {
        public static final String DATA_PATH_REFRESH_FIREBASE_TOKEN = "api/v4/delivery/fcm";
        public static final String FIREBASE_UPDATE_DEVICE_TOKEN = "deviceId";
        public static final String FIREBASE_UPDATE_USER_ID = "userId";
    }

    public interface SiteShotRequest {
        public static final String DATA_PATH_SITE_SHOT_HISTORY = "api/v1/campaigns/siteshot";
        public static final String DATA_PATH_ON_DEMAND_SUPPLY_ALLOTED = "api/v3/delivery/supply/";
        public static final String DATA_PATH_ON_DEMAND_SUPPLY_ACCEPTED = "api/v3/delivery/supply/";
        public static final String DATA_PATH_ON_DEMAND_SUPPLY_STATUS_UPDATE = "api/v3/delivery/supply/request/status";
        public static final String DATA_PATH_STATUS_UPDATE = "api/v3/delivery/order/status";
        public static final String SITE_SHOT_DATA_LIST_NAME = "object";

        public static final String DELIVERY_REQUEST_ID = "id";
        public static final String DELIVERY_AMOUNT = "orderAmount";
        public static final String DELIVERY_PAYMENT_MODE = "paymentMode";
        public static final String DELIVERY_CART_ID = "id";
        public static final String DELIVERY_SUPPLIER_NAME = "supplierName";
        public static final String DELIVERY_CUSTOMER_NAME = "customerName";
        public static final String DELIVERY_CUSTOMER_PHONE = "customerPhone";
        public static final String DELIVERY_SUPPLIER_LAT = "latitude";
        public static final String DELIVERY_SUPPLIER_LANG = "longitude";
        public static final String CART = "cart";
        public static final String STATUS = "state";
        public static final String DESCRIPTION = "description";
        public static final String STATUS_ID = "id";

        public static final String BRAND_NAME = "brandName";
        public static final String BRAND_LOGO_IMAGE = "logoThumbnailUrl";
        public static final String MEDIA = "mediaDto";
        public static final String MEDIA_IMAGE = "imageUrl";


        public static final String SITE_SHOT_LATITUDE = "lattitude";
        public static final String SITE_SHOT_LONGITUDE = "longitude";
        public static final String DELIVERY_REQUEST_TIME = "createdDate";
        public static final String SITE_SHOT_DUE_TIME = "dueTime";


        public static final String SITE_SHOT_ID = "siteShotId";
        public static final String CROWED_USER_SITE_SHOT_ID = "siteShotId";
        public static final String SITE_SHOT_TIME = "siteshotTime";
        public static final String SITE_SHOT_STATUS = "state";
        public static final String SITE_SHOT_FULL_IMAGE = "fullImageUrl";
        public static final String SITE_SHOT_FOCUS_IMAGE = "focusImageUrl";
        public static final String USER_SITE_SHOT_ID = "siteshotUserRequestId";
        public static final String USER_SITE_SHOT_REQUEST_TIME = "requestTime";
        public static final String USER_SITE_SHOT_EXPIRE_TIME = "dueTime";


    }

    public interface SiteManager {
        public static final String DATA_PATH_SITE_MANAGER = "api/v1/site/market/";
        public static final String DATA_PATH_SITE_MANAGER_HOLD = "api/v1/site/hold";
        public static final String DATA_PATH_SITE_MANAGER_ALL = "api/v1/site/active";
        public static final String DATA_PATH_SITE_CREATE = "api/v1/site";
        public static final String DATA_PATH_MARKET = "api/v1/site/market";
        public static final String DATA_PATH_SITE_UPDATE = "api/v1/site/update/";
        public static final String DATA_PATH_SITE_STATUS_UPDATE = "api/v1/reservation";

        public static final String SITE_SHOT_USER_REQUEST_ID = "siteshotUserRequestId";
        public static final String CAMPAIGN_SITE = "campaignSiteDto";
        public static final String CAMPAIGN = "campaignDto";
        public static final String BRAND = "brandsDto";
        public static final String BRAND_NAME = "brandName";
        public static final String BRAND_LOGO_IMAGE = "logoThumbnailUrl";
        public static final String MEDIA = "mediaDto";
        public static final String MEDIA_IMAGE = "imageUrl";

        public static final String SITE_LATITUDE = "latitude";
        public static final String SITE_LONGITUDE = "longitude";
        public static final String SITE_NAME = "name";
        public static final String SITE_DISPLAY_TYPE = "siteDisplayType";
        public static final String SITE_IMAGE_URL = "photosUrl";
        public static final String SITE_MARKET_ID = "marketId";

        public static final String SITE_ID = "siteId";
        public static final String START_DATE = "startDate";
        public static final String END_DATE = "endDate";
        public static final String COMMENT = "comment";
        public static final String STATUS = "state";
        public static final String MARKET_NAME = "name";
        public static final String MARKET_ID = "market_id";
    }

    //Bookmark Of Site

    public interface BookmarkDatabase {
        public static final String DATABASE_NAME = "skout.db";
        public static final String SITE_TABLE = "site_bookmark";
        public static final String SITE_ID = "id";
        public static final String SITE_NAME = "name";
        public static final String SITE_LATITUDE = "latitude";
        public static final String SITE_LONGITUDE = "longitude";
        public static final String SITE_DESCRIPTION = "description";
        public static final String SITE_TYPE = "site_type_id";
        public static final String SITE_DISPLAY_TYPE = "site_display_type";
        public static final String SITE_LOCATION_TYPE = "site_location_type_id";
        public static final String SITE_IMAGE_URL = "photos_url";
        public static final String SITE_BASE_PRICE = "base_price";
        public static final String SITE_FACING_DIRECTION = "facing_direction";
    }
}
