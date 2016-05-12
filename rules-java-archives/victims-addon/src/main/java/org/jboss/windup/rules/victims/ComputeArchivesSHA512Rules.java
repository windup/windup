package org.jboss.windup.rules.victims;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.ArchiveExtractionPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.config.ruleprovider.IteratingRuleProvider;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.rules.apps.java.scan.provider.UnzipArchivesToOutputRuleProvider;
import org.jboss.windup.util.exception.WindupException;
import org.ocpsoft.rewrite.config.ConditionBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Calculates SHA512 hash for each archive.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 *
 */
@RuleMetadata(tags = { "java" }, after = { UnzipArchivesToOutputRuleProvider.class }, phase = ArchiveExtractionPhase.class)
public class ComputeArchivesSHA512Rules extends IteratingRuleProvider<ArchiveModel>
{
    public static final String KEY_SHA512 = "SHA512";


    @Override
    public ConditionBuilder when()
    {
        return Query.fromType(ArchiveModel.class);
    }

    // @formatter:off
    @Override
    public void perform(GraphRewrite event, EvaluationContext context, ArchiveModel archive)
    {
        try (InputStream is = archive.asInputStream())
        {
            String hash = DigestUtils.sha512Hex(is);
            archive.asVertex().setProperty(KEY_SHA512, hash);
        }
        catch (IOException e)
        {
            throw new WindupException("Failed to read archive: " + archive.getFilePath() +
                "\n    Due to: " + e.getMessage(), e);
        }
    }
    // @formatter:on


    @Override
    public String toStringPerform()
    {
        return this.getClass().getSimpleName();
    }
}
