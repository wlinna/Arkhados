---
layout: post
title:  "Arkhados 0.6 released!"
date:   2016-01-07 22:58:00
---

Arkhados 0.6 has been released!

Improvements over 0.5.x:

- A new game mode: Team Deathmatch
  - Some heroes can now cast certain spells to the allies
- A new hero: Shadowmancer
- Better arena
- Animated lava
- Improvements on effects:
  - Added casting effects to many spells
  - Improved particle system so that trails don't have gaps
  -  Added visual and sound effects to many spells
- New music
- Added measures to prevent moving inside walls
- Bug fixes


I didn't make a trailer for Arkhados 0.6 so if you haven't seen the trailer for 0.5, I recommend you to check it out.

Here are some screenshots:

{% for data in site.data.screenshots.versions %}
  {% if data.version == '0.6' %}
  <div class="thumbnails">
    {% for image in data.screenshots %}
    <div class="thumbnail">
      <a href="{{ site.['baseurl'] }}/screenshots/{{ image }}">
        <img class="thumbnail-img" src="{{ site.['baseurl'] }}/screenshots/{{ image }}"
             alt="screenshot" />
      </a>
    </div>
    {% endfor %}
  </div>
  {% endif %}
{% endfor %}

<br />
[Download the latest release of Arkhados](https://github.com/TripleSnail/Arkhados/releases)
