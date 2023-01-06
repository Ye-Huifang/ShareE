package com.shareE.forum.controller;

import com.shareE.forum.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@Controller
public class DataController {

	@Autowired
	private DataService dataService;

	SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@RequestMapping(path = "/data", method = {RequestMethod.GET, RequestMethod.POST})
	public String getDataPage() {
		return "/site/admin/data";
	}

	@RequestMapping(path = "/data/uv", method = RequestMethod.POST)
	public String getUV(String start,
	                    String end, Model model) {
		long uv = 0;
		try {
			uv = dataService.calculateUV(simpleDateFormat.parse(start), simpleDateFormat.parse(end));
			model.addAttribute("uvResult", uv);
			model.addAttribute("uvStartDate", simpleDateFormat.parse(start));
			model.addAttribute("uvEndDate", simpleDateFormat.parse(end));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "forward:/data";
	}

	@RequestMapping(path = "/data/dau", method = RequestMethod.POST)
	public String getDAU(String start,
	                     String end, Model model) {
		long dau = 0;
		try {
			dau = dataService.calculateDAU(simpleDateFormat.parse(start), simpleDateFormat.parse(end));
			model.addAttribute("dauResult", dau);
			model.addAttribute("dauStartDate", simpleDateFormat.parse(start));
			model.addAttribute("dauEndDate", simpleDateFormat.parse(end));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return "forward:/data";
	}
}
