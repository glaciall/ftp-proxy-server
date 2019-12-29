package cn.org.hentai.ftp.proxy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by matrixy on 2019/4/10.
 */
public final class SessionAttributeSet
{
    Map<String, Object> attributes;
    public SessionAttributeSet()
    {
        this.attributes = new HashMap<String, Object>();
    }

    public SessionAttributeSet set(String key, Object value)
    {
        this.attributes.put(key, value);
        return this;
    }

    public Set<String> keys()
    {
        return this.attributes.keySet();
    }

    public <T> T get(String key)
    {
        return (T) this.attributes.get(key);
    }
}
