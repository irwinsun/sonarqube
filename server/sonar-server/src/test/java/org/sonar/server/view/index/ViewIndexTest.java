/*
 * SonarQube
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.view.index;

import java.util.List;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.sonar.api.config.Settings;
import org.sonar.server.es.EsTester;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.sonar.server.view.index.ViewIndexDefinition.INDEX;
import static org.sonar.server.view.index.ViewIndexDefinition.TYPE_VIEW;

public class ViewIndexTest {

  @ClassRule
  public static EsTester esTester = new EsTester().addDefinitions(new ViewIndexDefinition(new Settings()));

  private ViewIndex index;

  @Before
  public void setUp() {
    esTester.truncateIndices();
    index = new ViewIndex(esTester.client());
  }

  @Test
  public void find_all_view_uuids() throws Exception {
    ViewDoc view1 = new ViewDoc().setUuid("UUID1").setProjects(asList("P1"));
    ViewDoc view2 = new ViewDoc().setUuid("UUID2").setProjects(asList("P2"));
    esTester.index(INDEX, TYPE_VIEW, view1.uuid(), view1.getFields());
    esTester.index(INDEX, TYPE_VIEW, view2.uuid(), view2.getFields());

    List<String> result = newArrayList(index.findAllViewUuids());

    assertThat(result).containsOnly(view1.uuid(), view2.uuid());
  }

  @Test
  public void not_find_all_view_uuids() {
    List<String> result = newArrayList(index.findAllViewUuids());

    assertThat(result).isEmpty();
  }

  @Test
  public void delete_views() throws Exception {
    ViewDoc view1 = new ViewDoc().setUuid("UUID1").setProjects(asList("P1"));
    ViewDoc view2 = new ViewDoc().setUuid("UUID2").setProjects(asList("P2", "P3", "P4"));
    ViewDoc view3 = new ViewDoc().setUuid("UUID3").setProjects(asList("P2", "P3", "P4"));
    esTester.index(INDEX, TYPE_VIEW, view1.uuid(), view1.getFields());
    esTester.index(INDEX, TYPE_VIEW, view2.uuid(), view2.getFields());
    esTester.index(INDEX, TYPE_VIEW, view3.uuid(), view3.getFields());

    index.delete(asList(view1.uuid(), view2.uuid()));

    assertThat(esTester.getDocumentFieldValues(INDEX, TYPE_VIEW, ViewIndexDefinition.FIELD_UUID)).containsOnly(view3.uuid());
  }

}
