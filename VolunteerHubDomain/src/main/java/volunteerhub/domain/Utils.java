package volunteerhub.domain;

import org.mindrot.jbcrypt.BCrypt;

/** utils is a class for things that don't necessarily fit in the other classes
 * One method printMethodName() used in the tests to see the class.method being tested
 * One class for password hashing and password checking*/

public class Utils {

    public static void printMethodName() {
    StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
        // get running thread, and gets the 2nd (3rd) chain of call in progress
        // 0 = Thread.getStackTrace / 1= Utils.printMethodName / 2= volunteerhub.domain.UserTest.test....
        // caller object now holds our class name and method name within the structure of StackTraceElement

    System.out.println(caller.getClassName() + "." + caller.getMethodName());
        // call getClassName() and getMethodName() on caller (defined in class StackTraceElement)
    }

    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}