package root.util.DealSensitiveDataAOP;

public class MaskUtil {
    public static String mask(String original,MaskType type) {
        if (original==null){
            return null;
        }
        switch (type){
            case EMAIL:
                return maskEmail(original);
            case PHONE:
                return maskPhone(original);
            case ID_CARD:
                return maskIdCard(original);
            default:
                return maskDefault(original);
        }
    }

    private static String maskEmail(String email) {
        if (email.contains("@")){
            String[] parts = email.split("@");
            String name=parts[0];
            if(name.length()<=1){
                name="*";
            }else{
                name=name.charAt(0)+"***";
            }
            return name+"@"+parts[1];
        }
        return maskDefault(email);
    }

    private static String maskPhone(String phone) {
        if (phone.length()>=7){
            return phone.substring(0,3)+"****"+phone.substring(7);
        }
        return maskDefault(phone);
    }

    private static String maskIdCard(String idCard) {
        if (idCard.length() >= 10) {
            return idCard.substring(0, 3) + "********" + idCard.substring(idCard.length() - 2);
        }
        return maskDefault(idCard);
    }

    private static String maskDefault(String str) {
        if (str.length() <= 2) {
            return "*".repeat(str.length());
        }
        return str.charAt(0) + "***" + str.substring(str.length() - 1);
    }
}
