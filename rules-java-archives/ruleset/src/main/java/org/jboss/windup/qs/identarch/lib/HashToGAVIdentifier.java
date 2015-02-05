package org.jboss.windup.qs.identarch.lib;

import org.jboss.windup.qs.identarch.model.GAV;

/**
 *
 * @author Ondrej Zizka, ozizka at redhat.com
 */
public interface HashToGAVIdentifier
{
    public GAV getGAVFromSHA1(String sha1Hash);

    static final int SHA1_LENGTH = 40;
    static final int MAX_ENTRY_LENGTH = 250;
}
