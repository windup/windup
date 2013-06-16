/*
 * Copyright (c) 2013 Red Hat, Inc. and/or its affiliates.
 *  
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *  
 *  Contributors:
 *      Brad Davis - bradsdavis@gmail.com - Initial API and implementation
*/
package org.jboss.windup.decorator.integration.mvn;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.windup.decorator.ChainingDecorator;
import org.jboss.windup.decorator.integration.mvn.resp.MavenCentralSHA1VersionResponseItem;
import org.jboss.windup.decorator.integration.mvn.resp.MavenCentralSHA1VersionResponseWrapper;
import org.jboss.windup.interrogator.impl.XmlInterrogator;
import org.jboss.windup.interrogator.util.KnownArchiveProfiler;
import org.jboss.windup.metadata.decoration.AbstractDecoration;
import org.jboss.windup.metadata.decoration.Hash;
import org.jboss.windup.metadata.decoration.Hash.HashType;
import org.jboss.windup.metadata.decoration.archetype.version.PomVersion;
import org.jboss.windup.metadata.type.XmlMetadata;
import org.jboss.windup.metadata.type.archive.ZipMetadata;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;


public class MavenCentralSHA1VersionDecorator extends ChainingDecorator<ZipMetadata> {
	private static final Log LOG = LogFactory.getLog(MavenCentralSHA1VersionDecorator.class);
	private static final String MAVEN_API_URL = "http://search.maven.org/solrsearch/select?q=1:{query}&rows=1&wt=json";
	private static final String MAVEN_API_FILEPULL_URL = "http://search.maven.org/remotecontent?filepath=";

	private KnownArchiveProfiler knownArchiveProfiler;
	private Boolean active;
	private XmlInterrogator pomInterrogator;

	public void setKnownArchiveProfiler(KnownArchiveProfiler knownArchiveProfiler) {
		this.knownArchiveProfiler = knownArchiveProfiler;
	}

	public void setActive(String active) {
		this.active = BooleanUtils.toBooleanObject(active);
	}

	public void setPomInterrogator(XmlInterrogator pomInterrogator) {
		this.pomInterrogator = pomInterrogator;
	}

	@Override
	public void processMeta(ZipMetadata meta) {
		if (!active) {
			return;
		}
		if (!knownArchiveProfiler.isExclusivelyKnownArchive(meta)) {
			return;
		}

		String sha1Hash = null;
		for (AbstractDecoration result : meta.getDecorations()) {
			if (result instanceof PomVersion) {
				LOG.debug("Already has version result: " + result.toString());
				return;
			}
			else if (result instanceof Hash) {
				if (((Hash) result).getHashType() == HashType.SHA1) {
					sha1Hash = ((Hash) result).getHash();
				}
			}
		}

		if (sha1Hash == null) {
			LOG.debug("No SHA-1 Hash found. Returning.");
			return;
		}
		LOG.info("No Version Found: " + meta.getRelativePath() + "; trying Maven Central");
		if (LOG.isDebugEnabled()) {
			LOG.debug("SHA1: " + sha1Hash);
		}
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.getMessageConverters().add(new MappingJacksonHttpMessageConverter());

		try {
			MavenCentralSHA1VersionResponseWrapper result = restTemplate.getForObject(MAVEN_API_URL, MavenCentralSHA1VersionResponseWrapper.class, sha1Hash);

			if (result != null && result.getResponse() != null && result.getResponse().getNumFound() > 0) {
				MavenCentralSHA1VersionResponseItem rsp = result.getResponse().getDocs()[0];
				String groupId = rsp.getGroupId();
				String artifactId = rsp.getArtifactId();
				String version = rsp.getVersion();

				String url = generateUrl(groupId, artifactId, version);

				// pull the POM from the URL.
				ClientHttpRequestFactory request = new SimpleClientHttpRequestFactory();
				try {
					ClientHttpRequest pomRequest = request.createRequest(new URI(url), HttpMethod.GET);
					ClientHttpResponse resp = pomRequest.execute();

					String outputDir = meta.getArchiveOutputDirectory().getAbsolutePath() + File.separator + "maven-remote";
					FileUtils.forceMkdir(new File(outputDir));

					File outputPath = new File(outputDir + File.separator + "pom.xml");
					IOUtils.copy(new InputStreamReader(resp.getBody()), new FileOutputStream(outputPath));

					XmlMetadata xmlMeta = new XmlMetadata();
					xmlMeta.setFilePointer(outputPath);
					xmlMeta.setArchiveMeta(meta);

					pomInterrogator.processMeta(xmlMeta);
					LOG.info("Fetched remote POM for: " + meta.getName());
				}
				catch (Exception e) {
					LOG.error("Exception fetching remote POM: " + url + "; skipping.", e);
				}
			}
			else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("No Version Information Found in Maven Central for: " + sha1Hash);
				}
			}
		}
		catch (Exception e) {
			LOG.error("Exception creating API call to Central Repo for POM: " + meta.getName() + "; skipping.", e);
		}
	}

	protected String generateUrl(String groupId, String artifactId, String version) {
		StringBuilder url = new StringBuilder(MAVEN_API_FILEPULL_URL);
		url.append(StringUtils.replace(groupId, ".", "/"));
		url.append("/" + artifactId);
		url.append("/" + version);
		url.append("/" + artifactId + "-" + version);
		url.append(".pom");

		return url.toString();
	}

}
