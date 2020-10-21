/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.app.server.handler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.validator.routines.EmailValidator;

import com.app.models.ClipServices;
import com.app.pojo.Users;
import com.app.util.AppParams;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.http.HttpServerRequest;
import io.vertx.rxjava.ext.web.RoutingContext;

public class RegisterHandler implements Handler<RoutingContext> {

	static ClipServices clipServices;

	@Override
	public void handle(RoutingContext routingContext) {

		routingContext.vertx().executeBlocking(future -> {
			try {
				HttpServerRequest httpServerRequest = routingContext.request();

				// Lấy tham số từ Request
				JsonObject jsonRequest = routingContext.getBodyAsJson();
				String name = jsonRequest.getString("name");
				String password = jsonRequest.getString("password");
				String email = jsonRequest.getString("email");
				String confirmPassword = jsonRequest.getString("confirmPassword");
				JsonObject data = new JsonObject();
				data.put("email", email);
				List<Users> list = clipServices.findAllByProperty("from Users where email = '" + email + "'", null, 0,
						Users.class, 0);
				// generate uuid:
				String uuid = UUID.randomUUID().toString().replace("-", "");
				// uuid.randomUUID sinh ra 36 ki tu
				boolean duplicate = false;
				if (list.size() > 0) {
					duplicate = true;
				}
				if (name.equals("") || name == null) {
					duplicate = true;
					routingContext.put(AppParams.RESPONSE_CODE, HttpResponseStatus.BAD_REQUEST.code());
					routingContext.put(AppParams.RESPONSE_MSG, HttpResponseStatus.BAD_REQUEST.reasonPhrase());
					data.put("message", "register failed, name can not be blank");
				} else if (!password.equals(confirmPassword)) {
					duplicate = true;
					routingContext.put(AppParams.RESPONSE_CODE, HttpResponseStatus.BAD_REQUEST.code());
					routingContext.put(AppParams.RESPONSE_MSG, HttpResponseStatus.BAD_REQUEST.reasonPhrase());
					data.put("message", "register failed, password and confirm password are not matched");
				} else if (!isValid(email)) {
					data.put("message", "register failed, email is not valid");
					routingContext.put(AppParams.RESPONSE_CODE, HttpResponseStatus.BAD_REQUEST.code());
					routingContext.put(AppParams.RESPONSE_MSG, HttpResponseStatus.BAD_REQUEST.reasonPhrase());
				} else if (duplicate) {
					data.put("message", "register failed, email is duplicated");
					routingContext.put(AppParams.RESPONSE_CODE, HttpResponseStatus.BAD_REQUEST.code());
					routingContext.put(AppParams.RESPONSE_MSG, HttpResponseStatus.BAD_REQUEST.reasonPhrase());
				} else if (!duplicate && isValid(email)) {
					Users newUser = new Users(uuid, name, email, password);
					DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
					Date date = new Date();
					newUser.setCreatedAt(date);
					clipServices.save(newUser, uuid, Users.class, 0);
					data.put("message", "register successed");
					routingContext.put(AppParams.RESPONSE_CODE, HttpResponseStatus.CREATED.code());
					routingContext.put(AppParams.RESPONSE_MSG, HttpResponseStatus.CREATED.reasonPhrase());
				} else {
					data.put("message", "register failed");
					routingContext.put(AppParams.RESPONSE_CODE, HttpResponseStatus.BAD_REQUEST.code());
					routingContext.put(AppParams.RESPONSE_MSG, HttpResponseStatus.BAD_REQUEST.reasonPhrase());
				}
				routingContext.put(AppParams.RESPONSE_DATA, data);
				future.complete();
			} catch (Exception e) {
				routingContext.fail(e);
			}
		}, asyncResult -> {
			if (asyncResult.succeeded()) {
				routingContext.next();
			} else {
				routingContext.fail(asyncResult.cause());
			}
		});
	}

	public static void setClipServices(ClipServices clipServices) {
		RegisterHandler.clipServices = clipServices;
	}

	public static boolean isValid(String email) {
		boolean valid = false;
		valid = EmailValidator.getInstance().isValid(email);
		return valid;
	}
}
