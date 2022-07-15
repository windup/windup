package org.jboss.windup.arquillian;

import org.jboss.arquillian.container.spi.client.deployment.DeploymentDescription;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.forge.arquillian.spi.AddonDeploymentScenarioEnhancer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Deployment service that deploys the rexster addon everytime debugging is on.
 *
 * @author <a href="mailto:mbriskar@gmail.com">Matej Briškár</a>
 */
public class WindupFurnaceAddonDeploymentEnhancer implements AddonDeploymentScenarioEnhancer {
    @Override
    public List<DeploymentDescription> enhance(TestClass testClass, List<DeploymentDescription> deployments) {
        if (Boolean.getBoolean("maven.surefire.debug")) {
            String version = getWindupVersion(deployments);
            if (version != null) {
//                AddonId id = AddonId.from("org.jboss.windup.rexster:windup-rexster", version);
//                AddonDeploymentArchive archive = ShrinkWrap.create(AddonDeploymentArchive.class).setAddonId(id);
//
//                archive.setDeploymentTimeoutUnit(TimeUnit.MILLISECONDS);
//                archive.setDeploymentTimeoutQuantity(10000);
//
//                DeploymentDescription deploymentDescription = new DeploymentDescription(id.toCoordinates(), archive);
//                deploymentDescription.shouldBeTestable(false);
//                deployments.add(deploymentDescription);
            }
        }
        return deployments;
    }

    /**
     * Take the windup-config version and if not found, take the most frequent version of windup addons.
     *
     * @param deployments
     * @return
     */
    private String getWindupVersion(List<DeploymentDescription> deployments) {
        Map<String, Integer> versionOccurences = new HashMap<>();
        for (DeploymentDescription deployment : deployments) {

            if (deployment.toString().contains("windup")) {
                String version = deployment.toString().split(",")[1];

                if (deployment.toString().contains("windup-config")) {
                    return version;
                }
                if (versionOccurences.containsKey(deployment.toString().split(",")[1])) {
                    versionOccurences.put(version, versionOccurences.get(version) + 1);
                } else {
                    versionOccurences.put(version, 1);
                }
            }
        }
        Map.Entry<String, Integer> maxEntry = null;
        for (Map.Entry<String, Integer> stringIntegerEntry : versionOccurences.entrySet()) {
            if (maxEntry == null || stringIntegerEntry.getValue() > maxEntry.getValue()) {
                maxEntry = stringIntegerEntry;
            }
        }

        return maxEntry == null ? null : maxEntry.getKey();

    }
}
