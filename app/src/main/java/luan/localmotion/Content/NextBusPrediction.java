package luan.localmotion.Content;

public class NextBusPrediction {
    public String vID;
    public String routeTag;
    public String routeTitle;
    public String dirTitle;
    public String stopTitle;
    public Integer eta;

    public NextBusPrediction(String routeTag, String routeTitle, String dirTitle, String stopTitle, Integer eta) {
    	this.routeTag = routeTag;
        this.routeTitle = routeTitle;
    	this.dirTitle = dirTitle;
    	this.stopTitle = stopTitle;
    	this.eta = eta;
    }
    public NextBusPrediction(String vID, String routeTag, String routeTitle, String dirTitle, String stopTitle, Integer eta) {
        this.vID = vID;
        this.routeTag = routeTag;
        this.routeTitle = routeTitle;
        this.dirTitle = dirTitle;
        this.stopTitle = stopTitle;
        this.eta = eta;
    }
}
