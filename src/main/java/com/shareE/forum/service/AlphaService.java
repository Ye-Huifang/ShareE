package com.shareE.forum.service;

import com.shareE.forum.dao.AlphaDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
public class AlphaService {

	@Autowired
	private AlphaDao alphaDao;

	public AlphaService() {
		System.out.println("Constructor of AlphaService");
	}

	@PostConstruct
	public void init() {
		System.out.println("Init AlphaService");
	}

	@PreDestroy
	public void destroy() {
		System.out.println("Destroy AlphaService");
	}

	public String find() {
		return alphaDao.select();
	}
}
