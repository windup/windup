package org.jboss.windup.gui.components;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;

/**
 * Provides a graphical component that allows the user to select from a list of items to either include or exclude.
 *
 * @author jsightler
 */
public class IncludeExcludePickList extends JPanel
{
    private static final long serialVersionUID = 1L;
    private static final String ALL_ITEMS = "-- ALL ITEMS --";

    private boolean displayIncludeList = true;
    private JList<String> allList;
    private DefaultListModel<String> allListModel;
    private JList<String> includeList;
    private DefaultListModel<String> includeListModel;
    private JList<String> excludeList;
    private DefaultListModel<String> excludeListModel;

    private final JLabel allListDescription = new JLabel("All Items");
    private final JLabel includeListDescription = new JLabel("Include these Items");
    private final JLabel excludeListDescription = new JLabel("Exclude these Items");

    private final JButton includeAllButton = new JButton("Add All ->");
    private final JButton includeSelectedButton = new JButton("Add ->");
    private final JButton removeAllFromIncludeButton = new JButton("<- Remove All");
    private final JButton removeSelectedFromIncludeButton = new JButton("<- Remove");

    private final JButton excludeAllButton = new JButton("Add All ->");
    private final JButton excludeSelectedButton = new JButton("Add ->");
    private final JButton removeAllFromExcludeButton = new JButton("<- Remove All");
    private final JButton removeSelectedFromExcludeButton = new JButton("<- Remove");

    public IncludeExcludePickList()
    {
        initComponents();
    }

    public void setDisplayIncludeList(boolean displayIncludeList)
    {
        this.displayIncludeList = false;
        this.removeAll();
        initComponents();
    }

    public List<String> getIncludedItems()
    {
        return listModelToList(includeListModel);
    }

    public List<String> getExcludeItems()
    {
        return listModelToList(excludeListModel);
    }

    private List<String> listModelToList(ListModel<String> listModel)
    {
        List<String> result = new ArrayList<>(listModel.getSize());
        for (int i = 0; i < listModel.getSize(); i++)
        {
            String item = listModel.getElementAt(i);
            result.add(item);
        }
        return result;
    }

    public void setAllListDescription(String description)
    {
        allListDescription.setText(description);
    }

    public void setIncludeListDescription(String description)
    {
        includeListDescription.setText(description);
    }

    public void setExcludeListDescription(String description)
    {
        excludeListDescription.setText(description);
    }

    public void setAvailableItems(Collection<String> items)
    {
        this.allListModel.clear();
        for (String item : items)
        {
            this.allListModel.addElement(item);
        }
    }

    private void initComponents()
    {
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();

        setLayout(layout);

        this.allListModel = new DefaultListModel<>();
        this.allList = new JList<>(this.allListModel);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = .4;
        constraints.weighty = 1;
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.add(this.allListDescription, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(this.allList), BorderLayout.CENTER);
        add(leftPanel, constraints);

        JPanel centerPanel = createCenterButtonsPanel();
        constraints.weightx = .2;
        add(centerPanel, constraints);

        this.includeListModel = new DefaultListModel<>();
        this.includeListModel.addElement(ALL_ITEMS);
        this.includeList = new JList<>(this.includeListModel);
        this.excludeListModel = new DefaultListModel<>();
        this.excludeList = new JList<>(this.excludeListModel);

        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BorderLayout());

        JPanel selectedListsPanel = new JPanel();
        selectedListsPanel.setLayout(new BoxLayout(selectedListsPanel, BoxLayout.Y_AXIS));
        if (displayIncludeList)
        {
            rightPanel.add(this.includeListDescription, BorderLayout.NORTH);
            selectedListsPanel.add(new JScrollPane(this.includeList));
            selectedListsPanel.add(excludeListDescription);
        }
        else
        {
            rightPanel.add(this.excludeListDescription, BorderLayout.NORTH);
        }
        selectedListsPanel.add(new JScrollPane(this.excludeList));
        rightPanel.add(selectedListsPanel, BorderLayout.CENTER);

        constraints.weightx = .4;
        add(rightPanel, constraints);

        setupEventListeners();
    }

    private void setupEventListeners()
    {
        includeAllButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                includeListModel.removeElement(ALL_ITEMS);
                moveAllItems(allListModel, includeListModel);
            }
        });
        includeSelectedButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                includeListModel.removeElement(ALL_ITEMS);
                moveSelectedToList(allList, allListModel, includeListModel);
            }
        });
        removeAllFromIncludeButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                includeListModel.removeElement(ALL_ITEMS);
                moveAllItems(includeListModel, allListModel);
                includeListModel.addElement(ALL_ITEMS);
            }
        });
        removeSelectedFromIncludeButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                includeListModel.removeElement(ALL_ITEMS);
                moveSelectedToList(includeList, includeListModel, allListModel);
                if (includeListModel.size() == 0)
                {
                    includeListModel.addElement(ALL_ITEMS);
                }
            }
        });

        excludeAllButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                moveAllItems(allListModel, excludeListModel);
            }
        });
        excludeSelectedButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                moveSelectedToList(allList, allListModel, excludeListModel);
            }
        });
        removeAllFromExcludeButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                moveAllItems(excludeListModel, allListModel);
            }
        });
        removeSelectedFromExcludeButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent ae)
            {
                moveSelectedToList(excludeList, excludeListModel, allListModel);
            }
        });
    }

    private void moveAllItems(DefaultListModel<String> srcListModel, DefaultListModel<String> destListModel)
    {
        // destListModel.clear();
        for (int i = 0; i < srcListModel.size(); i++)
        {
            String item = srcListModel.elementAt(i);
            destListModel.addElement(item);
        }
        srcListModel.clear();
    }

    private void moveSelectedToList(JList<String> sourceList, DefaultListModel<String> srcListModel, DefaultListModel<String> destListModel)
    {
        int[] selectedIndices = sourceList.getSelectedIndices();
        for (int selectedIndex : selectedIndices)
        {
            String item = srcListModel.elementAt(selectedIndex);
            destListModel.addElement(item);
        }

        for (int i = selectedIndices.length - 1; i >= 0; i--)
        {
            srcListModel.removeElementAt(selectedIndices[i]);
        }
    }

    private JPanel createCenterButtonsPanel()
    {
        JPanel centerPanel = new JPanel();
        GridBagConstraints constraints = new GridBagConstraints();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        JPanel includeControlsJPanel = new JPanel();
        includeControlsJPanel.setLayout(new GridBagLayout());

        constraints.anchor = GridBagConstraints.NORTH;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.weightx = 0;
        constraints.weighty = .0;
        constraints.insets = new Insets(20, 10, 5, 10);

        includeControlsJPanel.add(includeAllButton, constraints);
        constraints.insets = new Insets(5, 10, 5, 10);
        includeControlsJPanel.add(includeSelectedButton, constraints);
        includeControlsJPanel.add(removeAllFromIncludeButton, constraints);
        includeControlsJPanel.add(removeSelectedFromIncludeButton, constraints);
        centerPanel.add(includeControlsJPanel);

        JPanel excludeControlsJPanel = new JPanel();
        excludeControlsJPanel.setLayout(new GridBagLayout());
        excludeControlsJPanel.add(excludeAllButton, constraints);
        constraints.insets = new Insets(5, 10, 5, 10);
        excludeControlsJPanel.add(excludeSelectedButton, constraints);
        excludeControlsJPanel.add(removeAllFromExcludeButton, constraints);
        excludeControlsJPanel.add(removeSelectedFromExcludeButton, constraints);
        centerPanel.add(excludeControlsJPanel);

        // constraints.weighty = 1;

        return centerPanel;
    }
}
