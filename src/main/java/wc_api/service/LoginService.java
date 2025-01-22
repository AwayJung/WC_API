package wc_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import wc_api.dao.LoginDAO;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final LoginDAO loginDAO;
}
