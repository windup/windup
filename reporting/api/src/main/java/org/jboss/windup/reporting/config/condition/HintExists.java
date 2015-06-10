package org.jboss.windup.reporting.config.condition;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.frames.structures.FramedVertexIterable;
import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.condition.GraphConditionFilter;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.rules.files.model.FileLocationModel;
import org.ocpsoft.rewrite.context.EvaluationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Returns true if there are {@link InlineHintModel} entries that match the given message text.
 *
 * @author <a href="mailto:jesse.sightler@gmail.com">Jesse Sightler</a>
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briskar</a>
 */
public class HintExists extends GraphConditionFilter<FileLocationModel>
{
    private String filename;
    private String messagePattern;



    HintExists(String messagePattern)
    {
        if(messagePattern != null)
        {
            this.messagePattern = "[\\s\\S]*" + messagePattern + "[\\s\\S]*";
        }
    }

    /**
     * Use the given message regular expression to match against {@link InlineHintModel#getHint()} property.
     */
    public static HintExists withMessage(String messagePattern)
    {
        return new HintExists(messagePattern);
    }

    /**
     * Only match {@link InlineHintModel}s that reference the given filename.
     */
    public HintExists in(String filename)
    {
        this.filename = filename;
        return this;
    }

    public HintExists from(String variableName)
    {
        setInputVariablesName(variableName);
        return this;
    }

    @Override public Iterable<? extends FileLocationModel> fillIn(GraphRewrite event, EvaluationContext context)
    {
        GraphService<? extends InlineHintModel> hintService = new GraphService<>(event.getGraphContext(),InlineHintModel.class);
        List<FileLocationModel> input = new ArrayList<>();
        for (InlineHintModel inlineHintModel : hintService.findAll())
        {
            input.add(inlineHintModel.getFileLocationReference());
        }
        return input;
    }


    /**
     * For the given payload check if it contains Hint matching the given settings
     * @param event
     * @param context
     * @param payload
     * @return
     */
    @Override public boolean accept(GraphRewrite event, EvaluationContext context, FileLocationModel payload)
    {
        boolean result = filename==null || (payload.getFile().getFileName().equals(filename));
        //get all hints for this fileLocation
        FramedVertexIterable<InlineHintModel> iterable = new FramedVertexIterable<InlineHintModel>(event.getGraphContext().getFramed(),payload.asVertex().getVertices(
                    Direction.IN, InlineHintModel.FILE_LOCATION_REFERENCE), InlineHintModel.class);
        result = result && iterable.iterator().hasNext();
        if(messagePattern != null) {
            boolean messageMatch =false;

            for (InlineHintModel hint : iterable)
            {
                messageMatch = messageMatch || hint.getHint().matches(messagePattern);
            }
            result = result && messageMatch;
        }
        return  result;
    }

    public String getFilename()
    {
        return filename;
    }

    public String getMessagePattern()
    {
        return messagePattern;
    }

}