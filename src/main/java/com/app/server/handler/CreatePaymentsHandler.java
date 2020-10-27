package com.app.server.handler;

import java.util.Date;
import java.util.List;

import com.app.models.ClipServices;
import com.app.pojo.Shipments;
import com.app.pojo.Transfer;
import com.app.pojo.Users;
import com.app.pojo.Wallets;
import com.app.session.redis.SessionStore;
import com.app.util.AppParams;
import com.google.gson.Gson;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.ext.web.Cookie;
import io.vertx.rxjava.ext.web.RoutingContext;

public class CreatePaymentsHandler implements Handler<RoutingContext>, SessionStore {
	static ClipServices clipServices;

	@SuppressWarnings("unchecked")
	@Override
	public void handle(RoutingContext routingContext) {
		routingContext.vertx().executeBlocking(future -> {
			try {
				JsonObject data = new JsonObject();
				JsonObject jsonRequest = routingContext.getBodyAsJson();
				Cookie c = routingContext.getCookie("sessionId");
				String sessionId = c.getValue();

				JsonArray listPay = jsonRequest.getJsonArray("listPay");
				Long total = jsonRequest.getLong("total");
				Gson gson = new Gson();
				Date date = new Date();
				Users loggedInUser = gson.fromJson(jedis.get(sessionId), Users.class);
				String userId = loggedInUser.getId();
				List<Wallets> listWallets = clipServices
						.findAllByProperty("from Wallets Where user_id ='" + userId + "'", null, 0, Wallets.class, 0);
				Wallets walletPay = new Wallets(); // walletPay = wallet of user loggin in
				if (listWallets.size() > 0) {
					walletPay = listWallets.get(0);
				}

				// compare available in wallet and total money need to pay
				if (total > walletPay.getBalance()) {
					data.put("availableBalance", walletPay.getBalance());
					data.put("invoiceDue", total);
					data.put("messsage",
							"You need to add $" + (total - walletPay.getBalance()) + " to pay the invoices");
				} else {
					// enough money to pay
					for (int i = 0; i < listPay.size(); i++) {
						String trackingCode = listPay.getJsonObject(i).getString("trackingCode");
						List<Shipments> shipments = clipServices.findAllByProperty(
								"from Shipments Where tracking_code ='" + trackingCode + "'", null, 0, Shipments.class,
								0);
						Shipments shipment = shipments.get(0);
						String shipmentId = shipment.getId();
						List<Transfer> transfers = clipServices.findAllByProperty(
								"from Transfer Where shipment_id ='" + shipmentId + "'", null, 0, Transfer.class, 0);
						Transfer transfer = transfers.get(0);
						String walletReceiveId = transfer.getToWalletId();
						Long amount = transfer.getAmount();
						List<Wallets> walletReceives = clipServices.findAllByProperty(
								"from Wallets Where id ='" + walletReceiveId + "'", null, 0, Wallets.class, 0);
						Wallets walletReceive = walletReceives.get(0);

						// update wallet of user who loggin in
						walletPay.setBalance(walletPay.getBalance() - total);
						walletPay.setDueAmount(walletPay.getDueAmount() - total);
						walletPay.setSpentAmount(walletPay.getSpentAmount() + total);
						walletPay.setUpdatedAt(date);
						clipServices.update(walletPay, walletPay.getId(), Wallets.class, 0);

						// update wallet of user whose get paid
						walletReceive.setBalance(walletReceive.getBalance() + amount);
						walletReceive.setUpdatedAt(date);
						clipServices.update(walletReceive, walletReceive.getId(), Wallets.class, 0);

						// update transfer
						transfer.setFinancialStatus("completed");
						transfer.setUpdatedAt(date);
						clipServices.update(transfer, transfer.getId(), Transfer.class, 0);

						// update shipment
						shipment.setFinancialStatus("completed");
						shipment.setUpdatedAt(date);
						shipment.setPaymentAt(date);
						clipServices.update(shipment, shipment.getId(), Shipments.class, 0);
					}
					data.put("availableBalance", walletPay.getBalance());
					data.put("invoiceDue", total);
					data.put("messsage", "done");
				}

				routingContext.put(AppParams.RESPONSE_CODE, HttpResponseStatus.OK.code());
				routingContext.put(AppParams.RESPONSE_MSG, HttpResponseStatus.OK.reasonPhrase());
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
		CreatePaymentsHandler.clipServices = clipServices;
	}

}