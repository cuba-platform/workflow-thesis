/*
* Copyright (c) 2009 Haulmont Technology Ltd. All Rights Reserved.
* Haulmont Technology proprietary and confidential.
* Use is subject to license terms.

* Author: Konstantin Krivopustov
* Created: 08.12.2009 11:37:15
*
* $Id$
*/
import com.haulmont.workflow.core.entity.Assignment
import com.haulmont.cuba.security.entity.User
import com.haulmont.cuba.core.SecurityProvider
import com.haulmont.cuba.core.global.ConfigProvider
import com.haulmont.cuba.core.global.GlobalConfig

Assignment a = assignment
User u = user
String link = makeLink(a)

if (SecurityProvider.currentUserSession().getLocale().getLanguage() == 'ru') {
  subject = "Новая задача: ${a.card.description} - ${a.card.locState}"

  body = """
<html><body>
Вы получили новую задачу: ${a.card.description} - ${a.card.locState}<br>
<a href="${link}">Перейти в карточку</a>
</body></html>
"""

} else {
  subject = "New assignment: ${a.card.description} - ${a.card.locState}"

  body = """
<html><body>
You've got an assignment: ${a.card.description} - ${a.card.locState}
<a href="${link}">Go to card</a>
</body></html>
"""
}

String makeLink(Assignment a) {
  GlobalConfig c = ConfigProvider.getConfig(GlobalConfig.class)
  return "http://${c.webHostName}:${c.webPort}/${c.webContextName}/open?" +
    "screen=wf\$Card.edit&" +
    "item=wf\$Card-${a.card.id}&" +
    "params=item:wf\$Card-${a.card.id}"
}