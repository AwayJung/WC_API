package wc_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import wc_api.dao.UserDAO;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserDAO userDAO;
}
