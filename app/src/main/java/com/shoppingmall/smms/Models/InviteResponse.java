package com.shoppingmall.smms.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class InviteResponse {
    @SerializedName("userId")
    @Expose
    public String UserId;

    @SerializedName("meetingId")
    @Expose
    public String MeetingId;

    @SerializedName("accept")
    @Expose
    public Boolean accept;

    @SerializedName("responseInvitation")
    @Expose
    public String responseInvitation;
}
