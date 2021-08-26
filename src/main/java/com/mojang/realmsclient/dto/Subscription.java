package com.mojang.realmsclient.dto;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.realmsclient.util.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Subscription extends ValueObject
{
    private static final Logger LOGGER = LogManager.getLogger();
    public long startDate;
    public int daysLeft;
    public Subscription.SubscriptionType type = Subscription.SubscriptionType.NORMAL;

    public static Subscription parse(String p_87673_)
    {
        Subscription subscription = new Subscription();

        try
        {
            JsonParser jsonparser = new JsonParser();
            JsonObject jsonobject = jsonparser.parse(p_87673_).getAsJsonObject();
            subscription.startDate = JsonUtils.getLongOr("startDate", jsonobject, 0L);
            subscription.daysLeft = JsonUtils.getIntOr("daysLeft", jsonobject, 0);
            subscription.type = typeFrom(JsonUtils.getStringOr("subscriptionType", jsonobject, Subscription.SubscriptionType.NORMAL.name()));
        }
        catch (Exception exception)
        {
            LOGGER.error("Could not parse Subscription: {}", (Object)exception.getMessage());
        }

        return subscription;
    }

    private static Subscription.SubscriptionType typeFrom(String p_87675_)
    {
        try
        {
            return Subscription.SubscriptionType.valueOf(p_87675_);
        }
        catch (Exception exception)
        {
            return Subscription.SubscriptionType.NORMAL;
        }
    }

    public static enum SubscriptionType
    {
        NORMAL,
        RECURRING;
    }
}
