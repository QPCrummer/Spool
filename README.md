![SpoolLogo1.png](SpoolLogo1.png)
# Spool
An embroidery, quilting, and sewing file manager!

## Installation
The following dependencies must be met:
- Java 25 or above
- Qt 6.4.2
- Python 3.9 or above
- pyembroidery python package

### How to install Qt
**Official Installation**
Follow the guide to install the launcher: [Qt Guide](https://www.qt.io/development/download-qt-installer)
**Note:** This appears to require an account. I am unsure if the account is free, but I assume it is.

**Unofficial Installation (easier, no account)**
Replace `{os}` with your os: linux, windows, macos.
If using windows, add `win64_msvc2019_64` after `6.4.2`
```shell
python3 -m pip install --user aqtinstall
python3 -m aqt install-qt {os} desktop 6.4.2 --outputdir ~/Qt
```
Then add `export PATH="$HOME/Qt/6.4.2/{os}/bin:$PATH"` before you launch script for the Spool jar

### How to install Pyembroidery
Simply run:
```shell
pip install --upgrade --force-reinstall git+https://github.com/QPCrummer/pystitch.git@main
```

## Features
- Support for rendering over 40 different file formats:
  - Embroidery:
    - 100
    - 10o
    - bro
    - dat
    - dsb
    - dst
    - dsz
    - emd
    - exp
    - exy
    - fxy
    - gcode
    - gt
    - hus
    - inb
    - jef
    - jpx
    - ksm
    - max
    - mit
    - new
    - pcd
    - pcm
    - pcq
    - pcs
    - pec
    - pes
    - phb 
    - phc
    - sew
    - shv
    - spx
    - stc
    - stx
    - tap
    - tbf
    - u01
    - vp3
    - xxx
    - zhs
    - zxy
    - edr
    - inf
    - pmv
    - iqp
  - Quilting
    - hqv
    - hqf
    - plt
    - qcc
  - General Purpose:
    - pdf
    - zip
- Add custom tags to files to sort and filter your files
- Search through files to find matches
- Export selected files directly to an attached USB drive (**WIP**)
- Upload files in bulk
- Convert between many different file formats