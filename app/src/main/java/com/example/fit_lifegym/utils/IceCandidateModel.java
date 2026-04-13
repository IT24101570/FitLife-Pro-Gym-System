package com.example.fit_lifegym.utils;

public class IceCandidateModel {
    private String sdpMid;
    private int sdpMLineIndex;
    private String sdp;

    public IceCandidateModel() {}

    public IceCandidateModel(String sdpMid, int sdpMLineIndex, String sdp) {
        this.sdpMid = sdpMid;
        this.sdpMLineIndex = sdpMLineIndex;
        this.sdp = sdp;
    }

    public String getSdpMid() { return sdpMid; }
    public void setSdpMid(String sdpMid) { this.sdpMid = sdpMid; }

    public int getSdpMLineIndex() { return sdpMLineIndex; }
    public void setSdpMLineIndex(int sdpMLineIndex) { this.sdpMLineIndex = sdpMLineIndex; }

    public String getSdp() { return sdp; }
    public void setSdp(String sdp) { this.sdp = sdp; }
}
