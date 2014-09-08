package org.jboss.windup.rules.apps.java.scan.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.IteratingRuleProvider;
import org.jboss.windup.config.RulePhase;
import org.jboss.windup.config.WindupRuleProvider;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * This RuleProvider gets the MD5 and SHA1 hash for each {@link ArchiveModel} in the graph.
 * 
 * @author jsightler <jesse.sightler@gmail.com>
 * 
 */
public class HashArchivesRuleProvider extends IteratingRuleProvider<ArchiveModel>
{

    @Override
    public ConditionBuilder when()
    {
        return Query.find(ArchiveModel.class);
    }

    @Override
    public RulePhase getPhase()
    {
        return RulePhase.DISCOVERY;
    }

    @Override
    public List<Class<? extends WindupRuleProvider>> getExecuteAfter()
    {
        return asClassList(UnzipArchivesToOutputRuleProvider.class);
    }

    @Override
    public void perform(GraphRewrite event, EvaluationContext context, ArchiveModel payload)
    {
        try (InputStream is = payload.asInputStream())
        {
            String md5 = DigestUtils.md5Hex(is);
            payload.setMD5Hash(md5);
        }
        catch (IOException e)
        {
            throw new WindupException("Failed to read archive file at: " + payload.getFilePath() + " due to: "
                        + e.getMessage(), e);
        }
        try (InputStream is = payload.asInputStream())
        {
            String sha1 = DigestUtils.sha1Hex(is);
            payload.setSHA1Hash(sha1);
        }
        catch (IOException e)
        {
            throw new WindupException("Failed to read archive file at: " + payload.getFilePath() + " due to: "
                        + e.getMessage(), e);
        }
    }
}
