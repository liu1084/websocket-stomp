package com.jim.im.utf.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by jim on 2017/6/21.
 * This class is ...
 */
@Controller
public class DashboardController {
	@RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
	public String index(){
		return "dashboard/index";
	}
}
