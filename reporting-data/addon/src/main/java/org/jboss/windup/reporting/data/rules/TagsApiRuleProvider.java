package org.jboss.windup.reporting.data.rules;

import org.jboss.windup.config.GraphRewrite;
import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.phase.ReportRenderingPhase;
import org.jboss.windup.config.tags.Tag;
import org.jboss.windup.config.tags.TagService;
import org.jboss.windup.config.tags.TagServiceHolder;
import org.jboss.windup.reporting.data.dto.TagDto;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@RuleMetadata(
        phase = ReportRenderingPhase.class,
        haltOnException = true
)
public class TagsApiRuleProvider extends AbstractApiRuleProvider {

    @Inject
    private TagServiceHolder tagServiceHolder;

    @Override
    public String getBasePath() {
        return "tags";
    }

    @Override
    public Object getAll(GraphRewrite event) {
        TagService tagService = tagServiceHolder.getTagService();
        return tagService.getAllTags().stream().map(tag -> {
                    TagDto tagDto = new TagDto();
                    tagDto.name = tag.getName();
                    tagDto.title = tag.getTitle();
                    tagDto.isRoot = tag.isPrime();
                    tagDto.isPseudo = tag.isPseudo();
                    tagDto.parentsTagNames = tag.getParentTags().stream()
                            .map(Tag::getName)
                            .collect(Collectors.toList());
                    return tagDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getById(GraphRewrite event) {
        return Collections.emptyMap();
    }
}
