package wc_api.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import wc_api.common.util.MessageUtil;

import java.util.Locale;

/**
 * 국제화(i18n/Message) 설정
 *
 * @author yrlee@mydata.re.kr
 */
@Configuration
public class MessageConfig implements WebMvcConfigurer {

    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource reloadableResourceBundleMessageSource = new ReloadableResourceBundleMessageSource();
        reloadableResourceBundleMessageSource.addBasenames("classpath:messages/messages");
        reloadableResourceBundleMessageSource.setDefaultEncoding("UTF-8");
        reloadableResourceBundleMessageSource.setCacheSeconds(60);
        return reloadableResourceBundleMessageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver acceptHeaderLocaleResolver = new AcceptHeaderLocaleResolver();
        acceptHeaderLocaleResolver.setDefaultLocale(Locale.KOREAN);
        return acceptHeaderLocaleResolver;
    }

    @Bean
    public MessageSourceAccessor messageSourceAccessor(ReloadableResourceBundleMessageSource reloadableResourceBundleMessageSource) {
        return new MessageSourceAccessor(reloadableResourceBundleMessageSource);
    }

    @Bean
    public MessageUtil message(MessageSourceAccessor messageSourceAccessor) {
        MessageUtil messageUtil = new MessageUtil();
        messageUtil.setMessageSourceAccessor(messageSourceAccessor);
        return messageUtil;
    }

}
