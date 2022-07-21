package net.suqatri.redicloud.commons;

public class ConditionChecks {

    public static boolean isInteger(String input){
        try{
            Integer.parseInt(input);
            return true;
        }catch(NumberFormatException e){
            return false;
        }
    }

    public static boolean isBoolean(String input){
        try{
            Boolean.parseBoolean(input);
            return true;
        }catch(NumberFormatException e){
            return false;
        }
    }

}
