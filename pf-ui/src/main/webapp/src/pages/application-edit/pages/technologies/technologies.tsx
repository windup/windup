import { useMemo, useState } from "react";
import { useOutletContext } from "react-router-dom";

import {
  Card,
  CardBody,
  CardTitle,
  Divider,
  EmptyState,
  EmptyStateIcon,
  EmptyStateVariant,
  Gallery,
  GalleryItem,
  Label,
  PageSection,
  SelectVariant,
  Title,
  Toolbar,
  ToolbarContent,
  ToolbarItem,
} from "@patternfly/react-core";
import FilterIcon from "@patternfly/react-icons/dist/esm/icons/filter-icon";
import InfoAltIcon from "@patternfly/react-icons/dist/esm/icons/info-alt-icon";
import { TableComposable, Tbody, Td, Tr } from "@patternfly/react-table";


import { ApplicationDto } from "@app/api/application";
import { TechnologyGroup } from "@app/api/technologies";
import { SimpleSelect, OptionWithValue } from "@app/shared/components";
import { useTechnologiesData } from "@app/shared/hooks";

type TechnologyGroupEnricherType = {
  [key in TechnologyGroup]: {
    color:
      | "blue"
      | "cyan"
      | "green"
      | "orange"
      | "purple"
      | "red"
      | "grey"
      | "gold";
  };
};

const technologyGroupsEnricherList: TechnologyGroupEnricherType = {
  View: {
    color: "blue",
  },
  Connect: {
    color: "green",
  },
  Store: {
    color: "orange",
  },
  Sustain: {
    color: "red",
  },
  Execute: {
    color: "purple",
  },
};

interface CardData {
  groupName: TechnologyGroup;
  technologyName: string;
  totalTagSum: number;
  tags: { [key: string]: number };
}

export const Technologies: React.FC = () => {
  const application = useOutletContext<ApplicationDto | null>();

  // Filters
  const [filterText] = useState("");
  const [technologyGroup, setTechnologyGroup] = useState<TechnologyGroup>();

  // Data
  const { applications, categoryOptions } = useTechnologiesData({
    applicationId: application?.id,
    hideEmptyCategoryOptions: true,
  });

  const cards = useMemo(() => {
    return applications
      .map((e) => e.technologyGroups)
      .map((group) => {
        return Object.entries(group)
          .map(([groupName, groupValue]) => {
            return Object.entries(groupValue).map(
              ([technologyName, technologyValue]) => {
                const { total, tags } = technologyValue;
                const cardData: CardData = {
                  groupName: groupName as TechnologyGroup,
                  technologyName: technologyName,
                  totalTagSum: total,
                  tags: tags,
                };
                return cardData;
              }
            );
          })
          .flatMap((e) => e);
      })
      .flatMap((e) => e)
      .filter((e) => e.totalTagSum > 0)
      .filter((e) => {
        let isFilterTextFilterCompliant = true;
        if (filterText && filterText.trim().length > 0) {
          isFilterTextFilterCompliant =
            e.technologyName.toLowerCase().indexOf(filterText.toLowerCase()) !==
            -1;
        }

        const isGroupCompliant = technologyGroup
          ? e.groupName === technologyGroup
          : true;

        return isFilterTextFilterCompliant && isGroupCompliant;
      })
      .sort((a, b) =>
        `${a.groupName}-${a.technologyName}`.localeCompare(
          `${b.groupName}-${b.technologyName}`
        )
      );
  }, [applications, filterText, technologyGroup]);

  return (
    <>
      <PageSection variant="light" type="nav">
        <Toolbar>
          <ToolbarContent>
            {/* <ToolbarItem variant="search-filter">
              <SearchInput value={filterText} onChange={setFilterText} />
            </ToolbarItem> */}
            <ToolbarItem>
              <SimpleSelect
                width={250}
                maxHeight={300}
                toggleIcon={<FilterIcon />}
                variant={SelectVariant.single}
                aria-label="category"
                aria-labelledby="category"
                placeholderText="Category"
                value={categoryOptions.find((e) => e.value === technologyGroup)}
                options={categoryOptions}
                onChange={(option) => {
                  const optionValue =
                    option as OptionWithValue<TechnologyGroup>;
                  setTechnologyGroup(optionValue.value);
                }}
                onClear={() => setTechnologyGroup(undefined)}
              />
            </ToolbarItem>
          </ToolbarContent>
        </Toolbar>
      </PageSection>
      <PageSection>
        <Gallery hasGutter minWidths={{ md: "400px" }}>
          {cards.map((card, index) => (
            <GalleryItem key={index}>
              <Card isFullHeight>
                <CardTitle>
                  {card.technologyName}{" "}
                  <Label
                    color={technologyGroupsEnricherList[card.groupName].color}
                  >
                    {card.groupName}
                  </Label>
                </CardTitle>
                <Divider />
                <CardBody>
                  <TableComposable variant="compact" borders={false}>
                    <Tbody>
                      {Object.keys(card.tags).length > 0 ? (
                        Object.entries(card.tags).map(
                          ([tagName, tagValue], tagIndex) => (
                            <Tr key={tagIndex}>
                              <Td>{tagName}</Td>
                              <Td className="pf-u-text-align-right">
                                {tagValue}
                              </Td>
                            </Tr>
                          )
                        )
                      ) : (
                        <EmptyState variant={EmptyStateVariant.small}>
                          <EmptyStateIcon icon={InfoAltIcon} />
                          <Title headingLevel="h4" size="md">
                            No data to show
                          </Title>
                        </EmptyState>
                      )}
                    </Tbody>
                  </TableComposable>
                </CardBody>
              </Card>
            </GalleryItem>
          ))}
        </Gallery>
      </PageSection>
    </>
  );
};
