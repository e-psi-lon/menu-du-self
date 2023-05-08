import os
import re
import requests
import asyncio

async def get_latest_release():
    url = 'https://api.github.com/repos/e-psi-lon/menu-du-self/releases/latest'
    response = requests.get(url)
    if response.status_code == 200:
        return response.json()
    else:
        return None

def get_version_name():
    path = os.path.join(os.getcwd(), 'app/build.gradle')     
    with open(path, 'r') as file:
        content = file.read()
        versionName = re.search(r'versionName "(.*)"', content).group(1)
        versionName = versionName.split('(')[0].strip()
    return versionName

def get_data_from_git():
    actual_hash = os.popen('git rev-parse --short HEAD').read().strip()
    previous_hash = os.popen('git rev-parse --short HEAD~1').read().strip()
    diff = os.popen(f'git diff --name-only {previous_hash} {actual_hash}').read().strip()
    diff = "- " + diff.replace('\n', '\n- ')
    return diff, actual_hash

async def main():
    latest_release = await get_latest_release() if await get_latest_release() is not None else {'tag_name': '0.5'}
    latest_tag = latest_release['tag_name']
    print(f'"{"LATEST_TAG"}={latest_tag}" >> $GITHUB_OUTPUT')
    print(f'"{"VERSION_NAME"}="{get_version_name()}"" >> $GITHUB_OUTPUT')
    diff, actual_hash = get_data_from_git()
    print(f'"{"CHANGELOG"}={diff}" >> $GITHUB_OUTPUT')
    print(f'"{"LAST_COMMIT_HASH"}={actual_hash}" >> $GITHUB_OUTPUT')

if __name__ == '__main__':
    asyncio.run(main())


