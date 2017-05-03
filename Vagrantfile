# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure(2) do |config|
  config.vm.box = "ubuntu/xenial64"

  config.vm.provider "virtualbox" do |vb|
    vb.cpus = 1
    vb.memory = 512
  end

  config.vm.provision 'install ruby', type: 'shell', inline: 'apt-get install -yy ruby ruby-dev', privileged: true
  config.vm.provision 'install travis', type: 'shell', inline: 'gem install travis -v 1.8.8 --no-rdoc -no-ri', privileged: true
end
