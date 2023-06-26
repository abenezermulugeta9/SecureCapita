/*******************************************************************************
 * @author Abenezer Sefinew
 * @version 1.0
 * @since 06/09/2023
 */

package com.abenezermulugeta.securecapita.utils;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.NoArgsConstructor;

import static com.twilio.rest.api.v2010.account.Message.creator;

@NoArgsConstructor
public class SMSUtils {
    /**
     * Once the three variables are assigned to the right twilio
     * API keys and phone number this will be functional
     * */
    private static String FROM_PHONE_NUMBER = "";
    private static String SID_KEY = "";
    private static String TOKEN_KEY = "";

    public static void sendSMS(String toPhoneNumber, String messageBody)  {
        Twilio.init(SID_KEY, TOKEN_KEY);
        Message message = creator(new PhoneNumber("+1" + toPhoneNumber), new PhoneNumber(FROM_PHONE_NUMBER), messageBody).create();
        System.out.println("SMS message==" + message);
    }
}
