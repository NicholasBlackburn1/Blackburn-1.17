package net.minecraft.client;

public enum CloudStatus
{
    OFF("options.off"),
    FAST("options.clouds.fast"),
    FANCY("options.clouds.fancy");

    private final String key;

    private CloudStatus(String p_167710_)
    {
        this.key = p_167710_;
    }

    public String getKey()
    {
        return this.key;
    }
}
