package com.xuxd.kafka.console.utils;

import com.xuxd.kafka.console.config.ContextConfigHolder;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.ScramMechanism;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2022-01-06 11:07:41
 **/
public class SaslUtil {

    public static final Pattern JAAS_PATTERN = Pattern.compile("^.*(username=\"(.*)\"[ \t]+).*$");

    private SaslUtil() {
    }

    public static String findUsername(String saslJaasConfig) {
        Matcher matcher = JAAS_PATTERN.matcher(saslJaasConfig);
        return matcher.find() ? matcher.group(2) : "";
    }

    public static boolean isEnableSasl() {
        Properties properties = ContextConfigHolder.CONTEXT_CONFIG.get().getProperties();
        if (!properties.containsKey(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG)) {
            return false;
        }
        String s = properties.getProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG);
        SecurityProtocol protocol = SecurityProtocol.valueOf(s);
        switch (protocol) {
            case SASL_SSL:
            case SASL_PLAINTEXT:
                return true;
            default:
                return false;
        }
    }


    public static boolean isEnableScram() {
        Properties properties = ContextConfigHolder.CONTEXT_CONFIG.get().getProperties();
        if (!properties.containsKey(SaslConfigs.SASL_MECHANISM)) {
            return false;
        }
        String s = properties.getProperty(SaslConfigs.SASL_MECHANISM);
        ScramMechanism mechanism = ScramMechanism.fromMechanismName(s);
        switch (mechanism) {
            case UNKNOWN:
                return false;
            default:
                return true;
        }
    }
}
