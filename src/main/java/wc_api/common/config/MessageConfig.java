package wc_api.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import wc_api.common.util.MessageUtil;

import java.util.Arrays;
import java.util.List;
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
        ReloadableResourceBundleMessageSource source = new ReloadableResourceBundleMessageSource();
        source.setBasename("classpath:/messages/messages"); // messages 폴더의 messages 파일을 기본으로
        source.setDefaultEncoding("UTF-8");
        source.setCacheSeconds(60);
        source.setFallbackToSystemLocale(false); // 시스템 로케일로 폴백하지 않음
        return source;
    }

    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.KOREAN);
        List<Locale> locales = Arrays.asList(Locale.KOREAN, Locale.ENGLISH);
        resolver.setSupportedLocales(locales); // 지원할 언어 설정
        return resolver;
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
