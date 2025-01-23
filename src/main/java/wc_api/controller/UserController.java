package wc_api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wc_api.service.UserService;

/**
 * 유저 컨트롤러
 *
 * @author 김창민
 */

@RestController
@RequestMapping("/api/login")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;



}
