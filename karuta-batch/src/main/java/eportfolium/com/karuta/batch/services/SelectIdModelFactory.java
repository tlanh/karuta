/* =======================================================
	Copyright 2020 - ePortfolium - Licensed under the
	Educational Community License, Version 2.0 (the "License"); you may
	not use this file except in compliance with the License. You may
	obtain a copy of the License at

	http://www.osedu.org/licenses/ECL-2.0

	Unless required by applicable law or agreed to in writing,
	software distributed under the License is distributed on an "AS IS"
	BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
	or implied. See the License for the specific language governing
	permissions and limitations under the License.
   ======================================================= */

// Based on Tapestry's SelectModelFactory.

package eportfolium.com.karuta.batch.services;

import java.util.List;

import org.apache.tapestry5.SelectModel;

/**
 * Used to create an {@link org.apache.tapestry5.SelectModel}.
 */
public interface SelectIdModelFactory
{   
    /**
     * Creates a {@link org.apache.tapestry5.SelectModel} from a list of objects of the same type and a label property name and an id property name.
     * The returned model creates for every object in the list a selectable option and relies on existing 
     * {@link org.apache.tapestry5.ValueEncoder} for the object type. The value of the label property is used as user-presentable label for the option.
     * The value of the id property is used as the hidden id for the option.
     * 
     * @param objects objects to create model from
     * @param labelProperty property for the client-side label
     * @param idProperty property for the client-side value
     * @return the model
     */
    public SelectModel create(List<?> objects, String labelProperty, String idProperty);
}
