package org.jboss.windup.reporting.data.rules;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.ReportPfRenderingPhase;
import org.jboss.windup.config.tags.Tag;
import org.jboss.windup.config.tags.TagService;
import org.jboss.windup.config.tags.TagServiceHolder;
import org.jboss.windup.reporting.data.dto.TagDto;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@RuleMetadata(
        phase = ReportPfRenderingPhase.class,
        haltOnException = true
)
public class TagsRuleProvider extends AbstractApiRuleProvider {

    public static final String PATH = "tags";

    @Inject
    private TagServiceHolder tagServiceHolder;

    @Override
    public String getBasePath() {
        return PATH;
    }

    @Override
    public Object getAll(GraphRewrite event) {
        TagService tagService = tagServiceHolder.getTagService();
        return tagService.getAllTags().stream().map(tag -> {
                    TagDto tagDto = new TagDto();
                    tagDto.setName(tag.getName());
                    tagDto.setTitle(tag.getTitle());
                    tagDto.setRoot(tag.isPrime());
                    tagDto.setPseudo(tag.isPseudo());
                    tagDto.setParentsTagNames(tag.getParentTags().stream()
                            .map(Tag::getName)
                            .collect(Collectors.toList())
                    );
                    return tagDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getById(GraphRewrite event) {
        return Collections.emptyMap();
    }
}
