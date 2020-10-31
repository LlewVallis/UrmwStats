package org.astropeci.urmwstats.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;

@RestController
public class LoginEndpoints {

    @GetMapping("/login")
    public RedirectView loginPage(HttpServletRequest request) {
        String referrer = request.getHeader("Referer");
        request.getSession().setAttribute("url_prior_login", referrer);

        return new RedirectView("/oauth2/authorization/discord");
    }
}
