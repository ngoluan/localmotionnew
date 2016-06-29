package luan.localmotion;

public class NextBusDashItem {
    public String vID;
    public String routeTag;
    public String routeTitle;
    public String dirTitle;
    public String stopTitle;
    public Integer eta;

    public NextBusDashItem(String routeTag, String routeTitle,String dirTitle, String stopTitle, Integer eta) {
    	this.routeTag = routeTag;
        this.routeTitle = routeTitle;
    	this.dirTitle = dirTitle;
    	this.stopTitle = stopTitle;
    	this.eta = eta;
    }
    public NextBusDashItem(String vID, String routeTag, String routeTitle,String dirTitle, String stopTitle, Integer eta) {
        this.vID = vID;
        this.routeTag = routeTag;
        this.routeTitle = routeTitle;
        this.dirTitle = dirTitle;
        this.stopTitle = stopTitle;
        this.eta = eta;
    }
}
