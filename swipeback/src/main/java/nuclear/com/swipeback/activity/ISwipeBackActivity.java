package nuclear.com.swipeback.activity;


import nuclear.com.swipeback.SwipeBackLayout;

/**
 * @author Yrom
 */
public interface ISwipeBackActivity {
    /**
     * @return the SwipeBackLayout associated with this activity.
     */
    public abstract SwipeBackLayout getSwipeBackLayout();

    public abstract void setSwipeBackEnable(boolean enable);

    /**
     * Scroll out contentView and finish the activity
     */
    public abstract void scrollToFinishActivity();

}
