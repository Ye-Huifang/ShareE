package com.shareE.forum.controller;

import com.shareE.forum.dao.AlphaDao;
import com.shareE.forum.service.AlphaService;
import com.shareE.forum.util.ForumUtil;
import org.apache.ibatis.reflection.ArrayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/alpha")
public class AlphaController {

	@Autowired
	private AlphaService alphaService;

	@RequestMapping("/hello")
	@ResponseBody
	public String sayHello() {
		return "Hello SpringBoot!";
	}

	@RequestMapping("/data")
	@ResponseBody
	public String getData() {
		return alphaService.find();
	}

	@RequestMapping("/http")
	public void http(HttpServletRequest request, HttpServletResponse response) {
		// get request data
		System.out.println(request.getMethod());
		System.out.println(request.getServletPath());
		Enumeration<String> enumeration = request.getHeaderNames();
		while (enumeration.hasMoreElements()) {
			String name = enumeration.nextElement();
			String value = request.getHeader(name);
			System.out.println(name + ": " + value);
		}
		System.out.println(request.getParameter("code"));

		// return response data
		response.setContentType("text/html;charset=utf-8");
		try (
			PrintWriter writer = response.getWriter();
		){
			writer.write("<h1>ShareE</h1>");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// GET request
	// /students?current=1&limit=20
	@RequestMapping(path = "/students", method = RequestMethod.GET)
	@ResponseBody
	public String getStudents(@RequestParam(name = "current", required = false, defaultValue = "1") int current,
	                          @RequestParam(name = "limit", required = false, defaultValue = "1") int limit) {
		System.out.println(current);
		System.out.println(limit);
		return "some students";
	}

	// GET
	// /student/123
	@RequestMapping(value = "/student/{id}", method = RequestMethod.GET)
	@ResponseBody
	public String getStudent(@PathVariable("id") int id) {
		System.out.println(id);
		return "a student";
	}

	// POST
	@RequestMapping(path = "/student", method = RequestMethod.POST)
	@ResponseBody
	public String saveStudent(String name, int age) {
		System.out.println(name);
		System.out.println(age);
		return "success";
	}

	// response HTML
	@RequestMapping(path = "/teacher", method = RequestMethod.GET)
	public ModelAndView getTeacher() {
		ModelAndView mav = new ModelAndView();
		mav.addObject("name", "zhangsan");
		mav.addObject("age", 30);
		mav.setViewName("/demo/view");
		return mav;
	}

	@RequestMapping(path = "/school", method = RequestMethod.GET)
	public String getSchool(Model model) {
		model.addAttribute("name", "UPenn");
		model.addAttribute("age", 80);
		return "/demo/view";
	}

	// response JSON (async)
	// Java Object -> JSON -> JS Object
	@RequestMapping(path = "/emp", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getEmp() {
		Map<String, Object> emp = new HashMap<>();
		emp.put("name", "zhangsan");
		emp.put("age", 20);
		emp.put("salary", 2000);
		return emp;
	}

	@RequestMapping(path = "/emps", method = RequestMethod.GET)
	@ResponseBody
	public List<Map<String, Object>> getEmps() {
		List<Map<String, Object>> list = new ArrayList<>();
		Map<String, Object> emp = new HashMap<>();
		emp.put("name", "zhangsan");
		emp.put("age", 20);
		emp.put("salary", 2000);

		Map<String, Object> emp2 = new HashMap<>();
		emp2.put("name", "wangwu");
		emp2.put("age", 25);
		emp2.put("salary", 2000);
		list.add(emp);
		list.add(emp2);
		return list;
	}

	@RequestMapping(path = "/cookie/set", method = RequestMethod.GET)
	@ResponseBody
	public String setCookie(HttpServletResponse response) {
		Cookie cookie = new Cookie("code", ForumUtil.generateUUID());
		cookie.setPath("/forum/alpha");
		cookie.setMaxAge(60 * 10);
		response.addCookie(cookie);
		return "set cookie";
	}

	@RequestMapping(path = "/cookie/get", method = RequestMethod.GET)
	@ResponseBody
	public String getCookie(@CookieValue("code") String code) {
		System.out.println(code);
		return "get cookie";
	}

	@RequestMapping(path = "/session/set", method = RequestMethod.GET)
	@ResponseBody
	public String setSession(HttpSession session) {
		session.setAttribute("id", 1);
		session.setAttribute("name", "Test");
		return "set session";
	}

	@RequestMapping(path = "/session/get", method = RequestMethod.GET)
	@ResponseBody
	public String getSession(HttpSession session) {
		System.out.println(session.getAttribute("id"));
		System.out.println(session.getAttribute("name"));
		return "get session";
	}

	// ajax example
	@RequestMapping(path = "/ajax", method = RequestMethod.POST)
	@ResponseBody
	public String testAjax(String name, int age) {
		System.out.println(name);
		System.out.println(age);
		return ForumUtil.getJSONString(0, "success!");
	}
}
