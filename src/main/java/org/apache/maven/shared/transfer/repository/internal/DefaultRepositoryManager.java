package org.apache.maven.shared.transfer.repository.internal;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.shared.transfer.artifact.ArtifactCoordinate;
import org.apache.maven.shared.transfer.internal.Delegator;
import org.apache.maven.shared.transfer.repository.RepositoryManager;

/**
 *
 */
@Singleton
@Named
class DefaultRepositoryManager
    extends Delegator<RepositoryManagerDelegate>
    implements RepositoryManager
{
  private final RepositoryManagerDelegate delegate;

  @Inject
  public DefaultRepositoryManager(final Map<String, RepositoryManagerDelegate> delegates) {
    super(delegates);
    this.delegate = selectDelegate();
  }

  @Override
  public String getPathForLocalArtifact(final ProjectBuildingRequest buildingRequest,
                                        final Artifact artifact)
  {
    return delegate.getPathForLocalArtifact(buildingRequest, artifact);
  }

  @Override
  public String getPathForLocalArtifact(final ProjectBuildingRequest buildingRequest,
                                        final ArtifactCoordinate coordinate)
  {
    return delegate.getPathForLocalArtifact(buildingRequest, coordinate);
  }

  @Override
  public String getPathForLocalMetadata(final ProjectBuildingRequest buildingRequest,
                                        final ArtifactMetadata metadata)
  {
    return delegate.getPathForLocalMetadata(buildingRequest, metadata);
  }

  @Override
  public ProjectBuildingRequest setLocalRepositoryBasedir(final ProjectBuildingRequest request, final File basedir) {
    return delegate.setLocalRepositoryBasedir(request, basedir);
  }

  @Override
  public File getLocalRepositoryBasedir(final ProjectBuildingRequest request) {
    return delegate.getLocalRepositoryBasedir(request);
  }
}
