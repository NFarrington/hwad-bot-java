package xyz.nowiknowmy.hogwarts.controllers;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ErrorController extends BasicErrorController {

    public ErrorController(ErrorAttributes errorAttributes, ServerProperties serverProperties, List<ErrorViewResolver> errorViewResolvers) {
        super(errorAttributes, serverProperties.getError(), errorViewResolvers);
    }

//    @RequestMapping(produces = MediaType.TEXT_HTML_VALUE)
//    @Override
//    public ModelAndView errorHtml(HttpServletRequest request,
//                                  HttpServletResponse response) {
//        HttpStatus status = getStatus(request);
//        Map<String, Object> model = Collections.unmodifiableMap(getErrorAttributes(
//            request, isIncludeStackTrace(request, MediaType.TEXT_HTML)));
//        response.setStatus(status.value());
//        ModelAndView modelAndView = resolveErrorView(request, response, status, model);
//        return (modelAndView != null) ? modelAndView : new ModelAndView("error", model);
//    }

}
