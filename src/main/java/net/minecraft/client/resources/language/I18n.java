package net.minecraft.client.resources.language;

import java.util.IllegalFormatException;
import net.minecraft.locale.Language;

public class I18n
{
    private static volatile Language language = Language.getInstance();

    private I18n()
    {
    }

    static void setLanguage(Language p_118942_)
    {
        language = p_118942_;
    }

    public static String m_118938_(String p_118939_, Object... p_118940_)
    {
        String s = language.getOrDefault(p_118939_);

        try
        {
            return String.format(s, p_118940_);
        }
        catch (IllegalFormatException illegalformatexception)
        {
            return "Format error: " + s;
        }
    }

    public static boolean exists(String pKey)
    {
        return language.has(pKey);
    }
}
